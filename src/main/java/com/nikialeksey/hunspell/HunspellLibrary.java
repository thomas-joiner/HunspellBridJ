package com.nikialeksey.hunspell;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface HunspellLibrary extends Library {

	int MAXWORDUTF8LEN = 256;

	/**
	 * <p>This will instantiate the Hunspell object and return a handle to it.</p>
	 * Original signature :
	 * <code>Hunhandle* Hunspell_create(const char*, const char*)</code><br>
	 * <i>native declaration : line 12</i>
	 * @param affpath the path to the affix file
	 * @param dpath the path to the dictionary file
	 */
	Pointer Hunspell_create(String affpath, String dpath);


	/**
	 * <p>This constructor must be used if the dictionary/affix files were compressed/encrypted
	 * using the Hunspell hzip program.</p>
	 * Original signature :
	 * <code>Hunhandle* Hunspell_create_key(const char*, const char*, const char*)</code>
	 * <br>
	 * <i>native declaration : line 14</i>
	 * @param affpath the path to the affix file
	 * @param dpath the path to the dictionary file
	 * @param key the key to decrypt encrypted dictionary files
	 */
	Pointer Hunspell_create_key(String affpath, String dpath, String key);

	/**
	 * <p>This calls the destructor on the Hunspell object.</p>
	 * Original signature : <code>void Hunspell_destroy(Hunhandle*)</code><br>
	 * <i>native declaration : line 17</i>
	 * @param pHunspell the handle on the hunspell object
	 */
	void Hunspell_destroy(Pointer pHunspell);

	/**
	* <p>Add dictionary (.dic file only)</p>
	* Original signature : <code>int Hunspell_add_dic(HunHandle*, const char*)</code><br>
	* @param pHunspell the handle on the hunspell object
	* @param dpath The path of the dictionary file
	* @return 0 if the dictionary was loaded, 1 if there are no available slots
	*/
	int Hunspell_add_dic(Pointer pHunspell, String dpath);

	/**
	 * <p>spellcheck word</p>
	 * Original signature :
	 * <code>int Hunspell_spell(Hunhandle*, const char*)</code><br>
	 * <i>native declaration : line 22</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param word the word to check
	 * @return 0 if incorrect, anything else is correct
	 */
	public int Hunspell_spell(Pointer pHunspell, byte[] word);

	/**
	 * <p>This retrieves the encoding of the dictionary that the Hunspell instance
	 * was instantiated with.</p>
	 * Original signature :
	 * <code>char* Hunspell_get_dic_encoding(Hunhandle*)</code><br>
	 * <i>native declaration : line 24</i>
	 * @param pHunspell the handle on the hunspell object
	 */
	String Hunspell_get_dic_encoding(Pointer pHunspell);

	/**
	 * <p>search suggestions</p>
	 * Original signature :
	 * <code>int Hunspell_suggest(Hunhandle*, char***, const char*)</code><br>
	 * <i>native declaration : line 33</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param slst an out param used to return the suggestions
	 * @param word the bad word that you need suggestions for
	 * @return the number of suggestions returned in the out param {@code slst}
	 */
	int Hunspell_suggest(Pointer pHunspell, PointerByReference slst, byte[] word);

	/**
	 * <p>morphological analysis of the word</p>
	 * Original signature :
	 * <code>int Hunspell_analyze(Hunhandle*, char***, const char*)</code><br>
	 * <i>native declaration : line 39</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param slst an out param used to return the analysis of the word
	 * @param word the word to analyze
	 * @return the number of suggestions returned in the out param {@code slst}
	 */
	int Hunspell_analyze(Pointer pHunspell, PointerByReference slst, byte[] word);

	/**
	 * <p>stemmer function</p>
	 * Original signature :
	 * <code>int Hunspell_stem(Hunhandle*, char***, const char*)</code><br>
	 * <i>native declaration : line 43</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param slst an out param used to return the possible stems of the word
	 * @param word the word to stem
	 * @return the number of suggestions returned in the out param {@code slst}
	 */
	int Hunspell_stem(Pointer pHunspell, PointerByReference slst, byte[] word);

	/**
	 * <p>get stems from a morph. analysis</p>
	 * Original signature :
	 * <code>int Hunspell_stem2(Hunhandle*, char***, char**, int)</code><br>
	 * <i>native declaration : line 52</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param slst an out param used to return the suggestions
	 * @param desc the output from {@link #Hunspell_analyze(Pointer, PointerByReference, byte[])} for the word you want to stem
	 * @param n the number of results in {@code desc}
	 * @return the number of suggestions returned in the out param {@code slst}
	 */
	int Hunspell_stem2(Pointer pHunspell, PointerByReference slst, Pointer desc, int n);

	/**
	 * <p>morphological generation by example(s)</p>
	 * Original signature :
	 * <code>int Hunspell_generate(Hunhandle*, char***, const char*, const char*)</code>
	 * <br>
	 * <i>native declaration : line 56</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param slst an out param used to return the possible stems of the word
	 * @param word the word to generate for
	 * @param word2 the word to use as an example to generate with
	 * @return the number of suggestions returned in the out param {@code slst}
	 */
	int Hunspell_generate(Pointer pHunspell, PointerByReference slst, byte[] word, byte[] word2);

	/**
	 * <p>generation by morph. description(s)</p>
	 * Original signature :
	 * <code>int Hunspell_generate2(Hunhandle*, char***, const char*, char**, int)</code>
	 * <br>
	 * <i>native declaration : line 67</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param slst an out param used to return the suggestions
	 * @param word the word to generate from
	 * @param desc the output from {@link #Hunspell_analyze(Pointer, PointerByReference, byte[])} for the word you want to use as an example for generation
	 * @param n the number of results in {@code desc}
	 * @return the number of suggestions returned in the out param {@code slst}
	 */
	int Hunspell_generate2(Pointer pHunspell, PointerByReference slst, byte[] word, Pointer desc, int n);

	/**
	 * <p>add word to the run-time dictionary</p>
	 * Original signature :
	 * <code>int Hunspell_add(Hunhandle*, const char*)</code><br>
	 * <i>native declaration : line 74</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param word the word to add to the runtime dictionary
	 */
	int Hunspell_add(Pointer pHunspell, byte[] word);

	/**
	 * <p>
	 * add word to the run-time dictionary with affix flags of the example (a
	 * dictionary word): Hunspell will recognize affixed forms of the new word,
	 * too.
	 * </p>
	 * Original signature:
	 * <code>int Hunspell_add_with_affix(Hunhandle*, const char*, const char*)</code>
	 * <br>
	 * <i>native declaration : line 81</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param word the word to add to the runtime dictionary
	 * @param example the word to use as an example to figure out which affix flags apply to the added word
	 * @return non-zero if error occurs
	 */
	int Hunspell_add_with_affix(Pointer pHunspell, byte[] word, byte[] example);

	/**
	 * <p>remove word from the run-time dictionary</p>
	 * Original signature :
	 * <code>int Hunspell_remove(Hunhandle*, const char*)</code><br>
	 * <i>native declaration : line 85</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param word the word to remove from the runtime dictionary
	 * @return non-zero if error occurs
	 */
	int Hunspell_remove(Pointer pHunspell, byte[] word);

	/**
	 * <p>free suggestion lists</p>
	 * Original signature :
	 * <code>void Hunspell_free_list(Hunhandle*, char***, int)</code><br>
	 * <i>native declaration : line 89</i>
	 * @param pHunspell the handle on the hunspell object
	 * @param slst the returned list that you want to clear
	 * @param n the number of items in the list
	 */
	void Hunspell_free_list(Pointer pHunspell, PointerByReference slst, int n);

}
