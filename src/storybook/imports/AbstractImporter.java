package storybook.imports;

import storybook.ui.MainFrame;

/**
 * todo: summary
 * @author kkoop64
 */
public abstract class AbstractImporter {

	protected MainFrame mainFrame;

	/**
	 * todo: summary
	 * @param mainFrame todo: summary
	 */
	public AbstractImporter(final MainFrame mainFrame) {

		this.mainFrame = mainFrame;
	}
}