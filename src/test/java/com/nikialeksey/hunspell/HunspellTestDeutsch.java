package com.nikialeksey.hunspell;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class HunspellTestDeutsch {

	private Hunspell unit;

	@Before
	public void setUp() throws URISyntaxException {
		String dicPath = new File(HunspellTestDeutsch.class.getResource("/de_DE.dic")
				.toURI()).getAbsolutePath();
		String affPath = new File(HunspellTestDeutsch.class.getResource("/de_DE.aff")
				.toURI()).getAbsolutePath();

		unit = new Hunspell(dicPath, affPath);
	}

	@After
	public void tearDown() {
		unit.close();
	}

	@Test
	public void testSpellWorksWithNonAsciiCharacters() {
		String[] correctWords = new String[] { "über", "Käse", "Fräulein" };
		
		for (String correctWord : correctWords) {
			assertThat(unit.spell(correctWord), is(true));
		}
	}
	
	@Test
	public void testSuggestionsProperlyEncoded() {
		assertThat(unit.suggest("Kaese"), hasItem("Käse"));
	}
	
	
	@Test
	public void testAnalysis() {
		assertThat(unit.analyze("Käse"), hasItem(" st:Käse"));
		assertThat(unit.analyze("Käse"), hasItem(" st:käsen fl:I"));
		assertThat(unit.analyze("Käse"), hasItem(" st:käse fl:k"));
	}

	@Test
	public void testStem() {
		assertThat(unit.stem("käse"), hasItem("käsen"));
	}
	
	@Test
	public void testStemThroughAnalyze() {
		assertThat(unit.stem(unit.analyze("käse")), hasItem("käsen"));
	}
	
	@Test
	public void testAdd() {
		String testWord = "äüäüä";
		
		assertThat(unit.spell(testWord), is(false));
		unit.add(testWord);
		assertThat(unit.spell(testWord), is(true));
	}
	
	@Test
	public void testAddDoesntAddAffixRules() {
		String testWord = "äüäüä";
		
		assertThat(unit.spell(testWord), is(false));
		unit.add(testWord);
		assertThat(unit.spell(testWord), is(true));
		assertThat(unit.spell(testWord+"s"), is(false));
	}
	
	@Test @Ignore(value = "apparent bug in hunspell")
	public void testRemove() {
		String testWord = "äüäüä";
		
		assertThat(unit.spell(testWord), is(false));
		unit.add(testWord);
		assertThat(unit.spell(testWord), is(true));
		unit.remove(testWord);
		assertThat(unit.spell(testWord), is(false));
	}
}
