package storybook.toolkit;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * todo: summary
 * @author kkoop64
 */
public class OpenNLP {

	private static final String ENGLISH_SENTENCE_DETECTION_OPEN_NLP_MODEL_PATH = "./src/storybook/resources/opennlpmodels/en-sent.bin";
	private static final String ENGLISH_NAME_FINDER_OPEN_NLP_MODEL_PATH = "./src/storybook/resources/opennlpmodels/en-ner-person.bin";
	private static final String ENGLISH_TOKENIZER_OPEN_NLP_MODEL_PATH = "./src/storybook/resources/opennlpmodels/en-token.bin";

	private SentenceModel sentenceModel;
	private TokenNameFinderModel tokenNameFinderModel;
	private TokenizerModel tokenizerModel;

	private SentenceDetectorME sentenceDetector;
	private NameFinderME nameFinder;
	private TokenizerME tokenizer;

	/**
	 * todo: summary
	 * @param importText todo: summary
	 * @return todo: summary
	 * @throws IOException
	 */
	public String[] detectSentences(final String importText) throws IOException {

		if (sentenceModel == null || sentenceDetector == null) {

			InputStream sentenceModelStream = new FileInputStream(ENGLISH_SENTENCE_DETECTION_OPEN_NLP_MODEL_PATH);

			sentenceModel = new SentenceModel(sentenceModelStream);

			sentenceDetector = new SentenceDetectorME(sentenceModel);

			sentenceModelStream.close();
		}

		return sentenceDetector.sentDetect(importText);
	}

	/**
	 * todo: summary
	 * @param sentence todo: summary
	 * @return todo: summary
	 * @throws IOException
	 */
	public Span[] findNamesInSentence(final String[] sentence) throws IOException {

		if (tokenNameFinderModel == null || tokenizer == null) {

			InputStream nameFinderModelStream = new FileInputStream(ENGLISH_NAME_FINDER_OPEN_NLP_MODEL_PATH);

			tokenNameFinderModel = new TokenNameFinderModel(nameFinderModelStream);

			nameFinder = new NameFinderME(tokenNameFinderModel);

			nameFinderModelStream.close();
		}

		return nameFinder.find(sentence);
	}

	/**
	 * todo: summary
	 * @param sentence todo: summary
	 * @return todo: summary
	 * @throws IOException
	 */
	public String[] tokenizeSentence(final String sentence) throws IOException {

		if (tokenizerModel == null || tokenizer == null) {

			InputStream tokenizerModelStream = new FileInputStream(ENGLISH_TOKENIZER_OPEN_NLP_MODEL_PATH);

			tokenizerModel = new TokenizerModel(tokenizerModelStream);

			tokenizerModelStream.close();

			tokenizer = new TokenizerME(tokenizerModel);
		}

		return tokenizer.tokenize(sentence);
	}
}
