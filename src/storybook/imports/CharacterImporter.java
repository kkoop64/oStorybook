package storybook.imports;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import com.sun.tools.hat.internal.util.Comparer;
import opennlp.tools.util.Span;
import org.hibernate.Session;
import storybook.model.BookModel;
import storybook.model.hbn.SbSessionFactory;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Person;
import storybook.toolkit.IOUtil;
import storybook.toolkit.OpenNLP;
import storybook.ui.MainFrame;

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

						// todo: make call to get gender

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
}