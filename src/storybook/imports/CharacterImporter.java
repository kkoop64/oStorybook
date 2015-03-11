package storybook.imports;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

import opennlp.tools.util.Span;
import storybook.toolkit.IOUtil;
import storybook.toolkit.OpenNLP;
import storybook.ui.MainFrame;

/**
 * todo: summary
 * @author kkoop64
 */
public class CharacterImporter extends AbstractImporter {

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

			Collections.addAll(names, Span.spansToStrings(nlp.findNamesInSentence(tokenizedSentence), tokenizedSentence));
		}

		// todo: add the names to the project characters list
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