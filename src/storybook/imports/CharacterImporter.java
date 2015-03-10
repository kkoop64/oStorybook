package storybook.imports;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
	public void importFromTxtFile() throws IOException {

		String[] importText = getImportFile();

		if (importText.length < 1)
			return;

		System.out.println("CharacterImporter.ImportCharactersFromTxtFile: Parsing import...");

		// todo: implement

		System.out.println("CharacterImporter.ImportCharactersFromTxtFile: Finished parsing import.");
	}

	/**
	 * todo: summary
	 * @return todo: summary
	 * @throws IOException
	 */
	private String[] getImportFile() throws IOException {

		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION)
			return new String[0];

		System.out.println("CharacterImporter.GetImportFile: Reading user selected import file...");

		File importFile = chooser.getSelectedFile();

		FileReader fileReader = new FileReader(importFile);
		BufferedReader importReader = new BufferedReader(fileReader);

		String currentLine;
		ArrayList<String> importText = new ArrayList<>();

		while ((currentLine = importReader.readLine()) != null)
			importText.add(currentLine);

		importReader.close();

		System.out.println("CharacterImporter.GetImportFile: Closed import file.");

		return importText.toArray(new String[importText.size()]);
	}
}
