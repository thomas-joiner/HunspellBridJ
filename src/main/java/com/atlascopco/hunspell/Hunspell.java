package com.atlascopco.hunspell;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.bridj.Pointer;

import com.atlascopco.hunspell.HunspellLibrary.Hunhandle;

/**
 * This class implements a object-oriented interface to the C API for Hunspell.
 * 
 * @author Thomas Joiner
 * 
 */
public class Hunspell implements Closeable {
	
	private Pointer<Hunhandle> handle;
	private Exception closedAt;
	
	/**
	 * Instantiate a hunspell object with the given dictionary and affix file
	 * @param dictionaryPath the path to the dictionary
	 * @param affixPath the path to the affix file
	 */
	public Hunspell(String dictionaryPath, String affixPath) {
		Pointer<Byte> affpath = Pointer.pointerToCString(affixPath);
		Pointer<Byte> dpath = Pointer.pointerToCString(dictionaryPath);
		
		handle = HunspellLibrary.Hunspell_create(affpath, dpath);
		
		if ( this.handle == null ) {
			throw new RuntimeException("Unable to instantiate Hunspell handle.");
		}
	}

	/**
	 * Spellcheck the given word.
	 * @param word the word to check
	 * @return true if it is spelled correctly
	 * @see HunspellLibrary#Hunspell_spell(Pointer, Pointer)
	 */
	public boolean spell(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		
		int result = HunspellLibrary.Hunspell_spell(handle, wordCString);
		
		return result != 0;
	}
	
	/**
	 * Same as {@link #spell(String)}
	 * @param word the word to check.
	 * @return true if it correct
	 * @see #spell(String)
	 */
	public boolean isCorrect(String word) {
		return spell(word);
	}

	/**
	 * Get the dictionary encoding for this object.
	 * @return the encoding for the dictionary
	 * @see HunspellLibrary#Hunspell_get_dic_encoding(Pointer)
	 */
	public String getDictionaryEncoding() {
		// check handle before attempting to operate on
		checkHandle();
		
		Pointer<Byte> dictionaryEncoding = HunspellLibrary.Hunspell_get_dic_encoding(handle);
		
		return dictionaryEncoding.getCString();
	}

	/**
	 * Suggest a list of corrections for the given word.
	 * @param word the word to get suggestions for
	 * @return the list of suggestions
	 * @see HunspellLibrary#Hunspell_suggest(Pointer, Pointer, Pointer)
	 */
	public List<String> suggest(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		
		int numResults = 0;
		
		List<String> suggestions = new ArrayList<String>();
		try {
			numResults = HunspellLibrary.Hunspell_suggest(handle, slst, wordCString);
		
			for ( int i = 0; i < numResults; i++) {
				suggestions.add(slst.get().get(i).getCString());
			}
		} finally {
			if ( slst != null ) {
				this.free_list(slst, numResults);
			}
		}
		
		return suggestions;
	}

	/**
	 * Morphological analysis of the given word.
	 * @param word the word to analyze
	 * @return the analysis
	 * @see HunspellLibrary#Hunspell_analyze(Pointer, Pointer, Pointer)
	 */
	public List<String> analyze(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		
		int numResults = 0;
		
		List<String> suggestions = new ArrayList<String>();
		try {
			numResults = HunspellLibrary.Hunspell_analyze(handle, slst, wordCString);
		
			for ( int i = 0; i < numResults; i++) {
				suggestions.add(slst.get().get(i).getCString());
			}
		} finally {
			if ( slst != null ) {
				this.free_list(slst, numResults);
			}
		}
		
		return suggestions;
	}

	/**
	 * Gets the stems of the word.
	 * @param word the word
	 * @return stems for the word
	 * @see HunspellLibrary#Hunspell_stem(Pointer, Pointer, Pointer)
	 */
	public List<String> stem(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		
		int numResults = 0;
		
		List<String> suggestions = new ArrayList<String>();
		try {
			numResults = HunspellLibrary.Hunspell_stem(handle, slst, wordCString);
		
			for ( int i = 0; i < numResults; i++) {
				suggestions.add(slst.get().get(i).getCString());
			}
		} finally {
			if ( slst != null ) {
				this.free_list(slst, numResults);
			}
		}
		
		return suggestions;
	}

	/**
	 * Gets the stems of a word from the results of {@link #analyze(String)}.
	 * @param analysis the results of {@link #analyze(String)}
	 * @return the stem information
	 * @see HunspellLibrary#Hunspell_stem2(Pointer, Pointer, Pointer, int)
	 */
	public List<String> stem(List<String> analysis) {
		// check handle before attempting to operate on
		checkHandle();
		
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		Pointer<Pointer<Byte>> analysisCStrings = Pointer.pointerToCStrings(analysis.toArray(new String[analysis.size()]));
		
		int numResults = 0;
		
		List<String> suggestions = new ArrayList<String>();
		try {
			numResults = HunspellLibrary.Hunspell_stem2(handle, slst, analysisCStrings, analysis.size());
		
			for ( int i = 0; i < numResults; i++) {
				suggestions.add(slst.get().get(i).getCString());
			}
		} finally {
			if ( slst != null ) {
				this.free_list(slst, numResults);
			}
		}
		
		return suggestions;
	}

	/**
	 * Generate a form for the first word based on the second word.
	 * @param word the word to generate the form for
	 * @param basedOn the word to base the generation on
	 * @return the generated form
	 * @see HunspellLibrary#Hunspell_generate(Pointer, Pointer, Pointer, Pointer)
	 */
	public List<String> generate(String word, String basedOn) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		checkWord("basedOn", basedOn);
		
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		Pointer<Byte> word2CString = Pointer.pointerToCString(basedOn);
		
		int numResults = 0;
		
		List<String> suggestions = new ArrayList<String>();
		try {
			numResults = HunspellLibrary.Hunspell_generate(handle, slst, wordCString, word2CString);
		
			for ( int i = 0; i < numResults; i++) {
				suggestions.add(slst.get().get(i).getCString());
			}
		} finally {
			if ( slst != null ) {
				this.free_list(slst, numResults);
			}
		}
		
		return suggestions;
	}

	/**
	 * Generate a form for the given word based on the analysis of a second word.
	 * @param word the word for which to generate the form
	 * @param basedOnAnalysis the analysis of the word that it is based on
	 * @return the generated form(s)
	 * @see HunspellLibrary#Hunspell_generate2(Pointer, Pointer, Pointer, Pointer, int)
	 */
	public List<String> generate(String word, List<String> basedOnAnalysis) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		Pointer<Pointer<Byte>> analysisCStrings = Pointer.pointerToCStrings(basedOnAnalysis.toArray(new String[basedOnAnalysis.size()]));
		
		int numResults = 0;
		
		List<String> suggestions = new ArrayList<String>();
		try {
			numResults = HunspellLibrary.Hunspell_generate2(handle, slst, wordCString, analysisCStrings, basedOnAnalysis.size());
		
			for ( int i = 0; i < numResults; i++) {
				suggestions.add(slst.get().get(i).getCString());
			}
		} finally {
			if ( slst != null ) {
				this.free_list(slst, numResults);
			}
		}
		
		return suggestions;
	}

	/**
	 * Add a word to the runtime dictionary.
	 * @param word the word to add
	 * @see HunspellLibrary#Hunspell_add(Pointer, Pointer)
	 */
	public void add(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		
		int result = HunspellLibrary.Hunspell_add(handle, wordCString);

		if ( result != 0 ) {
			throw new RuntimeException("An error occurred when calling Hunspell_add: "+result);
		}
	}

	/**
	 * Add the word to the runtime dictionary with the affix flags of the given
	 * example word so that affixed versions will be recognized as well.
	 * 
	 * @param word the word
	 * @param exampleWord a word that shows an example of what affix rules apply
	 * @see HunspellLibrary#Hunspell_add_with_affix(Pointer, Pointer, Pointer)
	 */
	public void addWithAffix(String word, String exampleWord) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		checkWord("exampleWord", exampleWord);
		
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		Pointer<Byte> example = Pointer.pointerToCString(exampleWord);
		
		int result = HunspellLibrary.Hunspell_add_with_affix(handle, wordCString, example);
		
		if ( result != 0 ) {
			throw new RuntimeException("An error occurred when calling Hunspell_add_with_affix: "+result);
		}
	}

	/**
	 * Remove a word from the runtime dictionary.
	 * 
	 * @param word the word to remove
	 * @see HunspellLibrary#Hunspell_remove(Pointer, Pointer)
	 */
	public void remove(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = Pointer.pointerToCString(word);
		
		int result = HunspellLibrary.Hunspell_remove(handle, wordCString);
		
		if ( result != 0 ) {
			throw new RuntimeException("An error occurred when calling Hunspell_remove: "+result);
		}
	}

	/**
	 * This method frees a list that Hunspell allocated.
	 * @param slst the list that hunspell allocated
	 * @param n the number of items in the list
	 * @see HunspellLibrary#Hunspell_free_list(Pointer, Pointer, int)
	 */
	private void free_list(Pointer<Pointer<Pointer<Byte>>> slst, int n) {
		HunspellLibrary.Hunspell_free_list(handle, slst, n);
	}
	
	/**
	 * Ensures the given word is not too long for the library to handle it
	 * @param parameterName the name of the parameter (for the error message)
	 * @param value the value of the parameter
	 */
	private void checkWord(String parameterName, String value) {
		if ( value.length() > HunspellLibrary.MAXWORDUTF8LEN ) {
			throw new IllegalArgumentException("Word '"+parameterName+"' greater than max acceptable length ("+HunspellLibrary.MAXWORDUTF8LEN+"): "+value);
		}
	}
	
	/**
	 * Checks the handle to make sure that it is still non-null.
	 */
	private void checkHandle() {
		if ( this.handle == null && this.closedAt != null ) {
			throw new IllegalStateException("This instance has already been closed.", closedAt);
		} else if ( this.handle == null ) {
			throw new IllegalStateException("Hunspell handle is null, but instance has not been closed.");
		}
	}

	/**
	 * This method will handle the destruction of the Hunspell instance and
	 * ensure that the memory is reclaimed.
	 */
	@Override
	public void close() {
		// Don't attempt to close multiple times
		if ( this.closedAt != null ) {
			return;
		}
		
		// Just in case the user has been messing with what they shouldn't
		if ( this.handle != null ) {
			HunspellLibrary.Hunspell_destroy(handle);
		} else {
			return;
		}
		
		this.handle = null;
		this.closedAt = new Exception();
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (this.closedAt!=null){
			this.close();
			System.err.println("Hunspell instance was not closed!");
		}
		
		super.finalize();
	}

}
