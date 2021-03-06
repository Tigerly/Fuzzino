//   Copyright 2012-2020 Fraunhofer FOKUS
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
package de.fraunhofer.fokus.fuzzing.fuzzino.util;

import de.fraunhofer.fokus.fuzzing.fuzzino.util.EncodedCharFinder.EncodedCharPosition;

public class StringEncoder {
	
	private static final int UTF16 = (int) Math.pow(2, 16);
	private static final int ASCII8BIT = (int) Math.pow(2,8);
	private static final int UTF32 = (int) Math.pow(2, 20) + UTF16;
	private static final int HIGH_SURROGATE_LOWER = 0xD800;
	private static final int HIGH_SURROGATE_UPPER = 0xDBFF;
	private static final int HIGH_SURROGATE_SHIFT = 10;
	private static final int LOW_SURROGATE_LOWER = 0xDC00;
	private static final int LOW_SURROGATE_UPPER = 0xDFFF;
	private static final int SURROGATE_OFFSET = 0x10000;

	public static int length(String str) {
		if (str == null) {
			return 0;
		}
		
		String replaced = str.replaceAll("\\\\x[0-9a-f]{2}", " ");
		replaced = replaced.replaceAll("\\\\u[0-9a-f]{4}", " ");
		replaced = replaced.replaceAll("\\\\U[0-9a-f]{8}", " ");
		
		return replaced.length();
	}

	/**
	 * Encodes all characters of a string according to the XML standard by using the schema
	 * {@code \\xnn} or {@code \\unnnn} or {@code \\Unnnnnnnn} 
	 * 
	 * @return
	 */
	public static String encode(String value) {
		if (value == null) {
			return null;
		}

		StringBuilder encodedString = new StringBuilder(value.length()*6);

		EncodedCharFinder entityFinder = new EncodedCharFinder(value);
		entityFinder.find();

		int nextStartIndex = 0;
		for (EncodedCharPosition position : entityFinder) {

			encodeSubstring(value, nextStartIndex, position.getStartIndex(), encodedString);

			encodedString.append(position.getEncodedCharString());

			nextStartIndex = position.getEndIndex()+1;
		}
		if (nextStartIndex < value.length()) {
			encodeSubstring(value, nextStartIndex, value.length(), encodedString);
		}

		return encodedString.toString();
	}

	public static String encodeChar(int c) {
		if (c < ASCII8BIT) {
			return "\\x" + (c < 0x10 ? "0" : "") + Integer.toHexString(c);
		}
		if (c < UTF16) {
			return "\\u" + (c < 0x1000 ? "0" : "") + Integer.toHexString(c);
		}
		if (c < UTF32) {
			return "\\U" + (c < 0x10000000 ? "0" : "") + (c < 0x01000000 ? "0" : "") + (c < 0x00100000 ? "0" : "") + Integer.toHexString(c);
		}
		return "" + c;
	}

	private static void encodeSubstring(String value, int startIndex, int endIndex, StringBuilder encodedString) {
		for (int i=startIndex; i<endIndex; i++) {
			char c = value.charAt(i);
			if (c >= HIGH_SURROGATE_LOWER && c <= HIGH_SURROGATE_UPPER && i < endIndex - 1) {
				int next = value.charAt(i+1);
				if (next >= LOW_SURROGATE_LOWER && next <= LOW_SURROGATE_UPPER) {
					// Ensure the next char is a low surrogate and we are not
					// dealing with bad unicode
					int codePoint = (c - HIGH_SURROGATE_LOWER) << HIGH_SURROGATE_SHIFT;
					codePoint += next - LOW_SURROGATE_LOWER;
					codePoint += SURROGATE_OFFSET;
					encodedString.append(encodeChar(codePoint));
					i++;
					continue;
				}
			}
			String encodedChar = encodeChar(c);
			encodedString.append(encodedChar);
		}
		
	}

}
