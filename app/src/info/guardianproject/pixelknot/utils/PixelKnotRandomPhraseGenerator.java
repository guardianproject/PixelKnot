package info.guardianproject.pixelknot.utils;

import info.guardianproject.pixelknot.Constants;

import java.util.ArrayList;
import java.util.Random;

import com.actionbarsherlock.app.SherlockFragment;

import android.content.Context;
import android.util.Log;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;

public class PixelKnotRandomPhraseGenerator implements SpellCheckerSessionListener {
	/**
	 * Spellchecker used to create random phrase
	 * 
	 * 1) target length: 24 <= tl.length <= 48
	 * 2) random bag of consonants, vowels
	 * 3) a few permutations to choose from [[c,v,v], [c,c,v], [v, c, v], [v, c, c]]
	 * 4) start a word according to random permutation
	 * 5) send through spellcheck; take first choice where choice.length >= 5 letters
	 * 6) repeat steps 4 and 5 until random_phrase.join(" ").length >= tl.length
	 * 
	 */
	
	public interface PixelKnotRandomPhraseGeneratorListener {
		public void onRandomPhraseGenerated(String random_phrase);
	};
	
	private static char[][] permutations = new char[][] {
			{'c', 'v', 'v', 'c', 'v'},
			{'c', 'c', 'v', 'c', 'c'},
			{'v', 'c', 'v', 'c', 'c'},
			{'v', 'c', 'c', 'c', 'v'},
			{'v', 'v', 'c', 'c', 'v'}
		};
	
	private static String vowel_bag = new String("aeiou");
	private char[] letter_bag = new char[26 - vowel_bag.length()];
	
	private Random r = new Random();
	private int target_length = r.nextInt(48 - 24) + 24;
	
	private SherlockFragment a;
	private ArrayList<String> random_phrase;
	private SpellCheckerSession spell_checker;
	
	private static String LOG = Constants.Logger.RPG;
	
	public PixelKnotRandomPhraseGenerator(SherlockFragment a) {
		this.a = a;
		random_phrase = new ArrayList<String>();
		
		char ch;
		int c = 0;
		
		for(ch = 'a'; ch <= 'z'; ch++) {
			if(vowel_bag.indexOf(ch) == -1) {
				letter_bag[c] = ch;
				c++;
			}
		}
		
		TextServicesManager tsm = (TextServicesManager) this.a.getActivity().getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
		spell_checker = tsm.newSpellCheckerSession(null, null, this, true);
	}	

	private static boolean evaluateRandomPhrase(ArrayList<String> random_phrase, int target_length) {
		int len = 0;
		for(String s : random_phrase) {
			len += s.length();
			if(len >= target_length) {
				return true;
			}
		}
		
		return false;
	}
	
	private String concatPhrase() {
		String random_phrase_concat = "";
		for(String s : random_phrase) {
			random_phrase_concat += (s + " ");
		}
		
		return random_phrase_concat.substring(0, random_phrase_concat.length() - 1);
	}
	
	public boolean buildRandomPhrase() {
		if(random_phrase.size() == 0 || !evaluateRandomPhrase(random_phrase, target_length)) {
			char[] permutation = permutations[r.nextInt(permutations.length - 1)];
			String seed_letters = "";
			for(char p : permutation) {
				if(p == 'c') {
					seed_letters += letter_bag[r.nextInt(letter_bag.length - 1)];
				} else {
					seed_letters += vowel_bag.charAt(r.nextInt(vowel_bag.length() - 1));
				}
			}
			
			spell_checker.getSuggestions(new TextInfo(seed_letters), 10);
			return false;
		}

		return true;
	}
	
	private void parseSuggestions(SuggestionsInfo r) {
		String random_word = null;
		
		for(int i=0; i<r.getSuggestionsCount(); i++) {
			String rw = r.getSuggestionAt(i);
			if(rw.length() >= 5) {
				random_word = rw;
				break;
			}
		}
		
		if(random_word == null) {
			return;
		}
		
		random_phrase.add(random_word);
		if(buildRandomPhrase()) {
			((PixelKnotRandomPhraseGeneratorListener) a).onRandomPhraseGenerated(concatPhrase());
		}
	}
	
	@Override
	public void onGetSuggestions(SuggestionsInfo[] results) {
		for(SuggestionsInfo r : results) {
			parseSuggestions(r);
		}
	}

	@Override
	public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {}
}
