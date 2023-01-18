package com.nikialeksey.hunspell;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

/**
 * This class implements an object-oriented interface to the C API for Hunspell.
 * 
 * @author Thomas Joiner
 * 
 */
public class Hunspell implements Closeable {

	private static final HunspellLibrary library = Native.load("hunspell", HunspellLibrary.class);
	private Pointer handle;
	private String encoding;
	private Exception closedAt;

	/**
	 * Instantiate a hunspell object with the given dictionary and affix file
	 * @param dictionaryPath the path to the dictionary
	 * @param affixPath the path to the affix file
	 */
	public Hunspell(String dictionaryPath, String affixPath) {
		handle = library.Hunspell_create(affixPath, dictionaryPath);
		encoding = library.Hunspell_get_dic_encoding(handle);
		
		if ( handle == null ) {
			throw new RuntimeException("Unable to instantiate Hunspell handle.");
		}
	}
	
	/**
	 * <p>
	 * Instantiate a hunspell object with the given hunzipped dictionary and
	 * affix files.
	 * </p>
	 * 
	 * <p>
	 * This is, however more complicated than it looks. Note that the paths
	 * aren't actually to the hunzipped dictionary and affix files, they are the
	 * paths to what they would be named if they weren't hunzipped. In other
	 * words, if you have the files {@code /path/to/dictionary.dic.hz} and
	 * {@code /path/to/dictionary.aff.hz} you would call
	 * {@code new Hunspell("/path/to/dictionary.dic", "/path/to/dictionary.aff", "password")}
	 * . Note, however, that if the paths that you give actually exist, those
	 * will be prioritized over the hunzipped versions and will be used instead.
	 * </p>
	 * 
	 * @param dictionaryPath the path to the dictionary
	 * @param affixPath the path to the affix file
	 * @param key the key used to encrypt the dictionary files
	 */
	public Hunspell(String dictionaryPath, String affixPath, String key) {
		handle = library.Hunspell_create_key(affixPath, dictionaryPath, key);
		encoding = library.Hunspell_get_dic_encoding(handle);
		
		if ( this.handle == null ) {
			throw new RuntimeException("Unable to instantiate Hunspell handle.");
		}
	}

	/**
	 * Spellcheck the given word.
	 * @param word the word to check
	 * @return true if it is spelled correctly
	 * @see HunspellLibrary#Hunspell_spell(Pointer, byte[])
	 */
	public boolean spell(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		int result = library.Hunspell_spell(handle, Native.toByteArray(word, encoding));
		
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
	
	private Charset getDictionaryCharset() {
		return Charset.forName(getDictionaryEncoding());
	}

	/**
	 * Get the dictionary encoding for this object.
	 * @return the encoding for the dictionary
	 * @see HunspellLibrary#Hunspell_get_dic_encoding(Pointer)
	 */
	public String getDictionaryEncoding() {
		// check handle before attempting to operate on
		checkHandle();
		
		return library.Hunspell_get_dic_encoding(handle);
	}

	/**
	 * Suggest a list of corrections for the given word.
	 * @param word the word to get suggestions for
	 * @return the list of suggestions
	 * @see HunspellLibrary#Hunspell_suggest(Pointer, PointerByReference, byte[])
	 */
	public List<String> suggest(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		PointerByReference slst = new PointerByReference();
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = library.Hunspell_suggest(handle, slst, Native.toByteArray(word, encoding));
		
			suggestions = encodedCStringListToStringList(slst, numResults);
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
	 * @see HunspellLibrary#Hunspell_analyze(Pointer, PointerByReference, byte[])
	 */
	public List<String> analyze(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);

		PointerByReference slst = new PointerByReference();
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = library.Hunspell_analyze(handle, slst, Native.toByteArray(word, encoding));
		
			suggestions = encodedCStringListToStringList(slst, numResults);
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
	 * @see HunspellLibrary#Hunspell_stem(Pointer, PointerByReference, byte[])
	 */
	public List<String> stem(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);

		PointerByReference slst = new PointerByReference();
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = library.Hunspell_stem(handle, slst, Native.toByteArray(word, encoding));
		
			suggestions = encodedCStringListToStringList(slst, numResults);
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
	 * @see HunspellLibrary#Hunspell_stem2(Pointer, PointerByReference, Pointer, int)
	 */
	public List<String> stem(List<String> analysis) {
		// check handle before attempting to operate on
		checkHandle();

		PointerByReference slst = new PointerByReference();
		Pointer analysisCStrings = toEncodedCStringList(analysis);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = library.Hunspell_stem2(handle, slst, analysisCStrings, analysis.size());
		
			suggestions = encodedCStringListToStringList(slst, numResults);
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
	 * @see HunspellLibrary#Hunspell_generate(Pointer, PointerByReference, byte[], byte[])
	 */
	public List<String> generate(String word, String basedOn) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		checkWord("basedOn", basedOn);

		PointerByReference slst = new PointerByReference();

		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = library.Hunspell_generate(handle, slst, Native.toByteArray(word, encoding), Native.toByteArray(basedOn, encoding));
		
			suggestions = encodedCStringListToStringList(slst, numResults);
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
	 * @see HunspellLibrary#Hunspell_generate2(Pointer, PointerByReference, byte[], Pointer, int)
	 */
	public List<String> generate(String word, List<String> basedOnAnalysis) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		PointerByReference slst = new PointerByReference();
		Pointer analysisCStrings = toEncodedCStringList(basedOnAnalysis);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = library.Hunspell_generate2(handle, slst, Native.toByteArray(word, encoding), analysisCStrings, basedOnAnalysis.size());
		
			suggestions = encodedCStringListToStringList(slst, numResults);
		} finally {
			if ( slst != null ) {
				this.free_list(slst, numResults);
			}
		}
		
		return suggestions;
	}

	/**
	 * Add an additional dictionary file (.dic file only, no affix file)
	 * to the runtime dictionary.
	 * @param dpath the Path to the dictionary file.
	 * @see HunspellLibrary#Hunspell_add_dic(Pointer, String)
	 */
	public void addDic(String dpath) {
		// check handle before attempting to operate on
		checkHandle();

		int result = library.Hunspell_add_dic(handle, dpath);

		if ( result != 0) {
			throw new RuntimeException("No available slot to add dictionary.");
		}
	}

	/**
	 * Add a word to the runtime dictionary.
	 * @param word the word to add
	 * @see HunspellLibrary#Hunspell_add(Pointer, byte[])
	 */
	public void add(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		int result = library.Hunspell_add(handle, Native.toByteArray(word, encoding));

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
	 * @see HunspellLibrary#Hunspell_add_with_affix(Pointer, byte[], byte[])
	 */
	public void addWithAffix(String word, String exampleWord) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		checkWord("exampleWord", exampleWord);
		
		int result = library.Hunspell_add_with_affix(handle, Native.toByteArray(word, encoding), Native.toByteArray(exampleWord, encoding));
		
		if ( result != 0 ) {
			throw new RuntimeException("An error occurred when calling Hunspell_add_with_affix: "+result);
		}
	}

	/**
	 * Remove a word from the runtime dictionary.
	 * 
	 * @param word the word to remove
	 * @see HunspellLibrary#Hunspell_remove(Pointer, byte[])
	 */
	public void remove(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		int result = library.Hunspell_remove(handle, Native.toByteArray(word, encoding));
		
		if ( result != 0 ) {
			throw new RuntimeException("An error occurred when calling Hunspell_remove: "+result);
		}
	}

	/**
	 * This method frees a list that Hunspell allocated.
	 * @param slst the list that hunspell allocated
	 * @param n the number of items in the list
	 * @see HunspellLibrary#Hunspell_free_list(Pointer, PointerByReference, int)
	 */
	private void free_list(PointerByReference slst, int n) {
		library.Hunspell_free_list(handle, slst, n);
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
			library.Hunspell_destroy(handle);
		} else {
			return;
		}
		
		this.handle = null;
		this.encoding = null;
		this.closedAt = new Exception();
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (this.closedAt == null){
			this.close();
			System.err.println("Hunspell instance was not closed!");
		}
		
		super.finalize();
	}
	
	/**
	 * Convert a list of strings to a list of cstrings in the
	 * dictionary encoding. 
	 * @param strings the strings to encode
	 * @return the pointer
	 */
	@SuppressWarnings("unchecked")
	public Pointer toEncodedCStringList(final List<String> strings) {
    	if (strings == null)
    		return null;

		return new StringArray(strings.toArray(new String[]{}), encoding);
    }
	
	/**
	 * Turn a list of cstrings encoded in the dictionary's charset to a Java list of strings.
	 * @param slst the results list
	 * @param numResults the number of results in the list
	 * @return the list of strings
	 */
	private List<String> encodedCStringListToStringList(PointerByReference slst, int numResults) {
		if (numResults == 0) {
			return Collections.emptyList();
		}

		List<String> strings = new ArrayList<String>();
		Pointer[] pointerArray = slst.getValue().getPointerArray(0, numResults);

		for ( int i = 0; i < numResults; i++) {
			strings.add(pointerArray[i].getString(0, encoding));
		}

		return strings;
	}

}
