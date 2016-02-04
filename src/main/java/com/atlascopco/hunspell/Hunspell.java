package com.atlascopco.hunspell;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bridj.Pointer;
import org.bridj.Pointer.Releaser;
import org.bridj.Pointer.StringType;
import org.bridj.PointerIO;

import com.atlascopco.hunspell.HunspellLibrary.Hunhandle;

/**
 * This class implements an object-oriented interface to the C API for Hunspell.
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
		Pointer<Byte> affpath = Pointer.pointerToCString(affixPath);
		Pointer<Byte> dpath = Pointer.pointerToCString(dictionaryPath);
		Pointer<Byte> keyCString = Pointer.pointerToCString(key);
		
		handle = HunspellLibrary.Hunspell_create_key(affpath, dpath, keyCString);
		
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
		
		Pointer<Byte> wordCString = toEncodedCString(word);
		
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
		
		Pointer<Byte> wordCString = toEncodedCString(word);
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = HunspellLibrary.Hunspell_suggest(handle, slst, wordCString);
		
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
	 * @see HunspellLibrary#Hunspell_analyze(Pointer, Pointer, Pointer)
	 */
	public List<String> analyze(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = toEncodedCString(word);
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = HunspellLibrary.Hunspell_analyze(handle, slst, wordCString);
		
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
	 * @see HunspellLibrary#Hunspell_stem(Pointer, Pointer, Pointer)
	 */
	public List<String> stem(String word) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Byte> wordCString = toEncodedCString(word);
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = HunspellLibrary.Hunspell_stem(handle, slst, wordCString);
		
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
	 * @see HunspellLibrary#Hunspell_stem2(Pointer, Pointer, Pointer, int)
	 */
	public List<String> stem(List<String> analysis) {
		// check handle before attempting to operate on
		checkHandle();

		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		Pointer<Pointer<Byte>> analysisCStrings = toEncodedCStringList(analysis);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = HunspellLibrary.Hunspell_stem2(handle, slst, analysisCStrings, analysis.size());
		
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
	 * @see HunspellLibrary#Hunspell_generate(Pointer, Pointer, Pointer, Pointer)
	 */
	public List<String> generate(String word, String basedOn) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		checkWord("basedOn", basedOn);
		
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		Pointer<Byte> wordCString = toEncodedCString(word);
		Pointer<Byte> word2CString = toEncodedCString(basedOn);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = HunspellLibrary.Hunspell_generate(handle, slst, wordCString, word2CString);
		
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
	 * @see HunspellLibrary#Hunspell_generate2(Pointer, Pointer, Pointer, Pointer, int)
	 */
	public List<String> generate(String word, List<String> basedOnAnalysis) {
		// check handle before attempting to operate on
		checkHandle();
		checkWord("word", word);
		
		Pointer<Pointer<Pointer<Byte>>> slst = Pointer.allocatePointerPointer(Byte.class);
		Pointer<Byte> wordCString = toEncodedCString(word);
		Pointer<Pointer<Byte>> analysisCStrings = toEncodedCStringList(basedOnAnalysis);
		
		int numResults = 0;
		
		List<String> suggestions = Collections.emptyList();
		try {
			numResults = HunspellLibrary.Hunspell_generate2(handle, slst, wordCString, analysisCStrings, basedOnAnalysis.size());
		
			suggestions = encodedCStringListToStringList(slst, numResults);
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
		
		Pointer<Byte> wordCString = toEncodedCString(word);
		
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
		
		Pointer<Byte> wordCString = toEncodedCString(word);
		Pointer<Byte> example = toEncodedCString(exampleWord);
		
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
		
		Pointer<Byte> wordCString = toEncodedCString(word);
		
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
	
	/**
	 * Returns the bytes for a cstring (null terminated string) in the
	 * encoding of the dictionary.
	 * 
	 * @param str the string to encode
	 * @return the encoded bytes
	 */
	private byte[] encodeCStringBytes(String str) {
        // get the encoded bytes of the string
		byte[] strBytes = str.getBytes(getDictionaryCharset());
		// allocate a buffer with one more byte so we can create a null-terminated
		// cstring
		byte[] cStringBytes = new byte[strBytes.length+1];
		
		// copy the encoded bytes into the cstring buffer
		System.arraycopy(strBytes, 0, cStringBytes, 0, strBytes.length);
		// ensure that the final byte is set to null
		cStringBytes[cStringBytes.length-1] = 0;
		
		return cStringBytes;
	}
	
	/**
	 * Returns a BridJ pointer to the encoded cstring of the
	 * provided string.
	 * @param str the string to encode
	 * @return the pointer
	 */
	private Pointer<Byte> toEncodedCString(String str) {
		byte[] cStringBytes = encodeCStringBytes(str);
		
		// convert it for use with BridJ
		Pointer<Byte> ptrBytes = Pointer.pointerToBytes(cStringBytes);
		
		return ptrBytes;
    }
	
	/**
	 * Convert a list of strings to a list of cstrings in the
	 * dictionary encoding. 
	 * @param strings the strings to encode
	 * @return the pointer
	 * @see Pointer#pointerToCStrings(String...)
	 */
	@SuppressWarnings("unchecked")
	public Pointer<Pointer<Byte>> toEncodedCStringList(final List<String> strings) {
    	if (strings == null)
    		return null;
        final int len = strings.size();
        final Pointer<Byte>[] pointers = (Pointer<Byte>[])new Pointer[len];
        Pointer<Pointer<Byte>> mem = Pointer.allocateArray(PointerIO.<Byte>getPointerInstance(Byte.class), len, new Releaser() {
        	//@Override
        	public void release(Pointer<?> p) {
        		Pointer<Pointer<Byte>> mem = (Pointer<Pointer<Byte>>)p;
				Charset dictionaryCharset = getDictionaryCharset();
        		for (int i = 0; i < len; i++) {
        			Pointer<Byte> pp = mem.get(i);
        			if (pp != null)
						strings.set(i, pp.getString(StringType.C, dictionaryCharset));
        			pp = pointers[i];
        			if (pp != null)
        				pp.release();
                }
        	}
        });
        for (int i = 0; i < len; i++)
            mem.set(i, pointers[i] = toEncodedCString(strings.get(i)));

		return mem;
    }
	
	/**
	 * Turn a cstring encoded in the dictionary's charset to a Java string.
	 * @param cString the cstring to decode
	 * @return the string 
	 */
	private String encodedCStringToJavaString(Pointer<Byte> cString) {
		return cString.getStringAtOffset(0, StringType.C, getDictionaryCharset());
	}
	
	/**
	 * Turn a list of cstrings encoded in the dictionary's charset to a Java list of strings.
	 * @param slst the results list
	 * @param numResults the number of results in the list
	 * @return the list of strings
	 */
	private List<String> encodedCStringListToStringList(Pointer<Pointer<Pointer<Byte>>> slst,
			int numResults) {
		List<String> strings = new ArrayList<String>();
		
		for ( int i = 0; i < numResults; i++) {
			strings.add(encodedCStringToJavaString(slst.get().get(i)));
		}
		
		return strings;
	}

}
