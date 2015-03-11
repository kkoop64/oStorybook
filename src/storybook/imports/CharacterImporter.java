package storybook.imports;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gson.Gson;
import org.hibernate.Session;

import storybook.model.BookModel;
import storybook.model.hbn.dao.GenderDAOImpl;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Person;
import storybook.toolkit.IOUtil;
import storybook.toolkit.OpenNLP;
import storybook.ui.MainFrame;

import opennlp.tools.util.Span;

/**
 * todo: summary
 * @author kkoop64
 */
public class CharacterImporter extends AbstractImporter {

	public static final ArrayList<String> namePrefixes = new ArrayList<String>() {{

		Collections.addAll(this, new String[]{

			// gender
			"Master", "Mr.", "Mister", "Mrs.", "Misses", "Miss",

			// title
			"Dr.", "Doctor", "Sir", "Count"
		});
	}};

	/**
	 * todo: summary
	 * @param mainFrame todo: summary
	 */
	public CharacterImporter(final MainFrame mainFrame) {

		super(mainFrame);
	}

	/**
	 * todo: summary
	 */
	public void importFromFile() throws IOException {

		// parse out the names

		OpenNLP nlp = new OpenNLP();

		String importText = getImportFileText();

		if (importText.length() < 1)
			return;

		String[] sentences = nlp.detectSentences(importText);

		ArrayList<String> names = new ArrayList<>();

		for (String sentence : sentences) {

			String[] tokenizedSentence = nlp.tokenizeSentence(sentence);

			String[] sentenceNames = Span.spansToStrings(nlp.findNamesInSentence(tokenizedSentence), tokenizedSentence);

			for (String name : sentenceNames)
				if (!names.contains(name))
					names.add(name);
		}

		// create new characters for the current project

		if (names.size() < 1)
			return;

		Comparator<String> namePartsLengthComparator = new Comparator<String>() {

			/**
			 * todo: summary
			 * @param o1 todo: summary
			 * @param o2 todo: summary
			 * @return todo: summary
			 */
			@Override
			public int compare(String o1, String o2) {

				String[] o1NameParts = o1.split("\\s+");
				String[] o2NameParts = o2.split("\\s+");

				return o2NameParts.length - o1NameParts.length;
			}
		};

		names.sort(namePartsLengthComparator); // ordering the names from longest to shortest helps detect duplicates

		BookModel model = mainFrame.getBookModel();

		ArrayList<Person> characters = new ArrayList<>();

		for (String name : names) {

			String[] nameParts = name.split("\\s+");

			if (nameParts.length > 0) {

				String firstName = "";
				String lastName = "";

				if (namePrefixes.contains(nameParts[0])) {

					if (nameParts.length == 2 && !nameParts[0].matches(".*\\d.*") && !nameParts[1].matches(".*\\d.*")) {

						firstName = nameParts[0]; // use the prefix
						lastName = nameParts[1];
					}
					else if (nameParts.length > 2 && !nameParts[0].matches(".*\\d.*") && !nameParts[nameParts.length-1].matches(".*\\d.*")) {

						firstName = nameParts[1];
						lastName = nameParts[nameParts.length-1];
					}
				}
				else if (!nameParts[0].matches(".*\\d.*") &&
					(
						nameParts.length == 1 ||
							!nameParts[nameParts.length-1].matches(".*\\d.*")
					)
					) {

					firstName = nameParts[0];

					if (nameParts.length > 1)
						lastName = nameParts[nameParts.length-1];
				}

				if (firstName.trim().length() > 0) {

					Boolean characterExists = false;

					for (Person character : characters) {

						if (character.getFirstname().trim().compareTo(firstName.trim()) == 0 &&
							(
								character.getLastname().trim().compareTo(lastName.trim()) == 0 ||
									lastName.trim().length() < 1
							)
							) {

							characterExists = true;
							break;
						}
					}

					if (!characterExists) {

						Person person = new Person();
						person.setFirstname(firstName.trim());
						person.setLastname(lastName.trim());

						Gender personGender = null;

						try {
							personGender = getNameGender(firstName);
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (personGender != null)
							person.setGender(personGender);

						characters.add(person);

						model.setNewPerson(person);
					}
				}
			}
		}
	}

	/**
	 * todo: summary
	 * @return todo: summary
	 * @throws IOException
	 */
	private String getImportFileText() throws IOException {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (fileChooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION)
			return "";

		File selectedImportFile = fileChooser.getSelectedFile();

		if (selectedImportFile.exists())
			return IOUtil.readFileAsString(selectedImportFile.toPath().toString());

		return "";
	}

	private Gender getNameGender(final String firstName) throws Exception {

		BookModel model = mainFrame.getBookModel();

		Session session = model.beginTransaction();

		GenderDAOImpl genderDao = new GenderDAOImpl(session);

		Gender male = genderDao.findMale();
		Gender female = genderDao.findFemale();

		model.commit();

		String json = readUrl("https://api.genderize.io/?name=" + URLEncoder.encode(firstName.trim()));

		Gson gson = new Gson();
		NameGender nameGender = gson.fromJson(json, NameGender.class);

		if (nameGender.gender != null) {

			if (nameGender.gender.compareTo("male") == 0)
				return male;
			else if (nameGender.gender.compareTo("female") == 0)
				return female;
		}

		return null;
	}

	private String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	static class NameGender {

		String name;
		String gender;
		String probability;
		int count;
	}
}