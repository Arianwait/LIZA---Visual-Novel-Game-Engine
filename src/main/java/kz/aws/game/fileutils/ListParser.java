package kz.aws.game.fileutils;

import java.util.List;

import kz.aws.game.character.ICharacter;

public class ListParser {
	public static int findIdInList(String personName, List<ICharacter> iCharacterList) {
		int i = 0;
		for(ICharacter iCharacter : iCharacterList) {
			if(iCharacter.getName().equals(personName)) {
				return i;
			} else {
				i++;
			}
		}
		return 99;
	}
}
