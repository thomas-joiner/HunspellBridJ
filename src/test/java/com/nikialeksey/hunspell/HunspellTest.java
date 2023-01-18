package com.nikialeksey.hunspell;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HunspellTest {

	private static final String LONG_WORD = "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf"
			+ "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf"
			+ "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf"
			+ "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf";

	private Hunspell unit;

	@Before
	public void setUp() throws URISyntaxException {
		String dicPath = new File(HunspellTest.class.getResource("/en_US.dic")
				.toURI()).getAbsolutePath();
		String affPath = new File(HunspellTest.class.getResource("/en_US.aff")
				.toURI()).getAbsolutePath();

		unit = new Hunspell(dicPath, affPath);
	}

	@After
	public void tearDown() {
		unit.close();
	}

	@Test
	public void testSpellWithCorrectWords() {
		String[] correctWords = { "words", "good", "Pegasus", "test" };

		for (String word : correctWords) {
			assertThat(word, unit.spell(word), is(true));
		}
	}

	@Test
	public void testSpellWithIncorrectWords() {
		String[] incorrectWords = { "wrods", "flase",
				"supercalifragilisticexpialidocious" };

		for (String word : incorrectWords) {
			assertThat(word, unit.spell(word), is(false));
		}
	}

	@Test
	public void testHzipConstructor() throws URISyntaxException {
		File parentFile = new File(HunspellTest.class.getResource("/en_US.aff")
				.toURI()).getParentFile();
		String dicPath = new File(parentFile, "test.dic").getAbsolutePath();
		String affPath = new File(parentFile, "test.aff").getAbsolutePath();

		Hunspell hunspell = null;
		try { 
			hunspell = new Hunspell(dicPath, affPath, "password");

			String[] correctWords = { "words", "good", "Pegasus", "test" };

			for (String word : correctWords) {
				assertThat(word, unit.spell(word), is(true));
			}
		} finally {
			hunspell.close();
		}
	}

	@Test
	public void testGetDictionaryEncoding() {
		assertThat(unit.getDictionaryEncoding(), is("UTF-8"));
	}
	
	@Test
	public void testSuggestions() {
		assertThat(unit.suggest("wrods"), hasItem("words"));
		assertThat(unit.suggest("flase"), hasItem("false"));
		assertThat(unit.suggest("supercalifragilisticexpialidocious"), hasItem("supercilious"));
	}
	
	@Test
	public void testAnalysis() {
		assertThat(unit.analyze("words"), hasItem(" st:word ts:0 is:Ns"));
		assertThat(unit.analyze("words"), hasItem(" st:word ts:0 is:Vs"));
		assertThat(unit.analyze("good"), hasItem(" st:good ts:0 al:best al:better"));
		assertThat(unit.analyze("Pegasus"), hasItem(" st:Pegasus ts:0"));
		assertThat(unit.analyze("test"), hasItem(" st:test ts:0"));
	}
	
	@Test
	public void testStem() {
		assertThat(unit.stem("words"), hasItem("word"));
		assertThat(unit.stem("good"), hasItem("good"));
		assertThat(unit.stem("Pegasus"), hasItem("Pegasus"));
		assertThat(unit.stem("test"), hasItem("test"));
	}
	
	@Test
	public void testStemThroughAnalyze() {
		assertThat(unit.stem(unit.analyze("words")), hasItem("word"));
		assertThat(unit.stem(unit.analyze("good")), hasItem("good"));
		assertThat(unit.stem(unit.analyze("Pegasus")), hasItem("Pegasus"));
		assertThat(unit.stem(unit.analyze("test")), hasItem("test"));
	}
	
	@Test
	public void testGenerate() {
		assertThat(unit.generate("words", "monkeys"), hasItem("words"));
		assertThat(unit.generate("good", "monkeys"), hasItem("goods"));
		assertThat(unit.generate("Pegasus", "monkeys"), hasItem("Pegasuses"));
		assertThat(unit.generate("test", "monkeys"), hasItem("tests"));
	}

	@Test
	public void testGenerateThroughAnalyze() {
		List<String> basedOn = unit.analyze("monkeys");
		assertThat(unit.generate("words", basedOn), hasItem("words"));
		assertThat(unit.generate("good", basedOn), hasItem("goods"));
		assertThat(unit.generate("Pegasus", basedOn), hasItem("Pegasuses"));
		assertThat(unit.generate("test", basedOn), hasItem("tests"));
	}
	
	@Test
	public void testAdd() {
		String testWord = "supercalifragilisticexpialidocious";
		
		assertThat(unit.spell(testWord), is(false));
		unit.add(testWord);
		assertThat(unit.spell(testWord), is(true));
	}
	
	@Test
	public void testAddDoesntAddAffixRules() {
		String testWord = "alacorn";
		
		assertThat(unit.spell(testWord), is(false));
		unit.add(testWord);
		assertThat(unit.spell(testWord), is(true));
		assertThat(unit.spell(testWord+"s"), is(false));
	}
	
	@Test
	public void testAddWithAffix() {
		String testWord = "alacorn";
		
		assertThat(unit.spell(testWord), is(false));
		unit.addWithAffix(testWord, "monkey");
		assertThat(unit.spell(testWord), is(true));
		assertThat(unit.spell(testWord+"'s"), is(true));
		assertThat(unit.spell(testWord+"s"), is(true));
		assertThat(unit.spell(testWord+"ed"), is(true));
		assertThat(unit.spell(testWord+"ing"), is(true));
	}
	
	@Test
	public void testRemove() {
		String testWord = "supercalifragilisticexpialidocious";
		
		assertThat(unit.spell(testWord), is(false));
		unit.add(testWord);
		assertThat(unit.spell(testWord), is(true));
		unit.remove(testWord);
		assertThat(unit.spell(testWord), is(false));
	}
	
	@Test
	public void testRemoveAfterAddWithAffix() {
		String testWord = "alacorn";
		
		assertThat(unit.spell(testWord), is(false));
		unit.addWithAffix(testWord, "monkey");
		assertThat(unit.spell(testWord), is(true));
		assertThat(unit.spell(testWord+"s"), is(true));
		unit.remove(testWord);
		assertThat(unit.spell(testWord), is(false));
		assertThat(unit.spell(testWord+"s"), is(false));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSpellRejectsWordsThatAreTooLong() {
		unit.spell(LONG_WORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSuggestRejectsWordsThatAreTooLong() {
		unit.suggest(LONG_WORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAnalyzeWordsThatAreTooLong() {
		unit.analyze(LONG_WORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStemRejectsWordsThatAreTooLong() {
		unit.stem(LONG_WORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGenerateWord1RejectsWordsThatAreTooLong() {
		unit.generate(LONG_WORD, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGenerateWord2RejectsWordsThatAreTooLong() {
		unit.generate("", LONG_WORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGenerateWithAnalysisWord1RejectsWordsThatAreTooLong() {
		unit.generate(LONG_WORD, Collections.<String> emptyList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddRejectsWordsThatAreTooLong() {
		unit.add(LONG_WORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddWithAffixRejectsWordsThatAreTooLong() {
		unit.addWithAffix(LONG_WORD, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddWithAffixExampleRejectsWordsThatAreTooLong() {
		unit.addWithAffix("", LONG_WORD);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testSpellThrowsExceptionAfterClose() throws IOException {
		unit.close();
		unit.spell(LONG_WORD);
	}

	@Test(expected = IllegalStateException.class)
	public void testSuggestThrowsExceptionAfterClose() {
		unit.close();
		unit.suggest(LONG_WORD);
	}

	@Test(expected = IllegalStateException.class)
	public void testAnalyzeThrowsExceptionAfterClose() {
		unit.close();
		unit.analyze(LONG_WORD);
	}

	@Test(expected = IllegalStateException.class)
	public void testStemThrowsExceptionAfterClose() {
		unit.close();
		unit.stem(LONG_WORD);
	}

	@Test(expected = IllegalStateException.class)
	public void testGenerateWord1ThrowsExceptionAfterClose() {
		unit.close();
		unit.generate(LONG_WORD, "");
	}

	@Test(expected = IllegalStateException.class)
	public void testGenerateWord2ThrowsExceptionAfterClose() {
		unit.close();
		unit.generate("", LONG_WORD);
	}

	@Test(expected = IllegalStateException.class)
	public void testGenerateWithAnalysisWord1ThrowsExceptionAfterClose() {
		unit.close();
		unit.generate(LONG_WORD, Collections.<String> emptyList());
	}

	@Test(expected = IllegalStateException.class)
	public void testAddThrowsExceptionAfterClose() {
		unit.close();
		unit.add(LONG_WORD);
	}

	@Test(expected = IllegalStateException.class)
	public void testAddWithAffixThrowsExceptionAfterClose() {
		unit.close();
		unit.addWithAffix(LONG_WORD, "");
	}

	@Test(expected = IllegalStateException.class)
	public void testAddWithAffixExampleThrowsExceptionAfterClose() {
		unit.close();
		unit.addWithAffix("", LONG_WORD);
	}
	
	@Test
	public void testMultipleClosesCauseNoError() throws IOException {
		unit.close();
		unit.close();
	}
	
	@Test
	public void testAddDictionary() throws URISyntaxException {
		String userDicPath = new File(HunspellTest.class.getResource("/user.dic")
				.toURI()).getAbsolutePath();
		unit.addDic(userDicPath);

		String[] correctWords = new String[] {"words", "good", "Pegasus", "test",
			"supercalifragilisticexpialidocious",
			"alacorn", "alacorns"};

		for (String word : correctWords) {
			assertThat(word, unit.spell(word), is(true));
		}
	}
}
