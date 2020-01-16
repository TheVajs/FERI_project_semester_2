package com.example.feriproject;

import java.util.ArrayList;
import java.util.List;

public class Compression {

	private static int bits;
	private static int bitCount;
	private static int currentByte;
	
	/*
	 *  COMPRESSION
	 */
	public static List<Byte> compress(String text) {
		int[] values = new int[text.length()];
		for(int i = 0; i < values.length; i++) {
			values[i] = (int)text.charAt(i);
		}
		return compress(values); 
	}
	public static List<Byte> compress(int[] values) {
		System.out.println("COMPRESSING");
		
		List<Byte> list = new ArrayList<>(); 
		List<Integer> differences = new ArrayList<>();
        list.add((byte)0);
		bits = 0;
		bitCount = 0;
		
		// DIFFERENCES
		differences.add(values[0]);
		for(int i = 1; i < values.length; i++) {
			differences.add(values[i] - values[i - 1]);
		}
		//out(values);
		//System.out.println(differences.toString());
		
		// 1. first value
		WriteNumber(values[0], 8, list);
		
		// 2.other
		int repetition = 0;
		for(int i = 1; i < differences.size(); i++) {
			if(differences.get(i) == 0) {
				repetition++;
			} else {
				if(repetition != 0) {
					// 01
					toListRepetition(repetition, list);
					repetition = 0;
				}
				if(Math.abs(differences.get(i)) > 30) { 
					// 10
					toListAbsolute(differences.get(i), list);
				}
				else {
					// 00
					toListDifference(differences.get(i), list);
				}
			}
		}
		if(repetition != 0) {
			// 01
			toListRepetition(repetition, list);
		}
		
		// 3. end 
		// 11
		WriteBit(1, list);
		WriteBit(1, list);	
		
		//out(list);
		
		// Write to file
		//list.toArray().toString();
		return list;
	}
	
	private static void toListRepetition(int repetition, List<Byte> list) {
		//System.out.println("Repetition: "+repetition);
		for(int i = 0; i < (int)(repetition/8); i++) {
		    WriteBit(1, list);
			WriteBit(0, list);
			WriteNumber(7, 3, list);
		}
	    WriteBit(1, list);
		WriteBit(0, list);
		WriteNumber(repetition % 8 - 1, 3, list);
		//System.out.println(getBitString(repetition % 8 - 1, 3));
	}
	private static void toListAbsolute(int value, List<Byte> list) {
		//System.out.println("Absolute: " + value);
	    WriteBit(0, list);
		WriteBit(1, list);
		WriteBit(value < 0 ? 1 : 0, list);
		WriteNumber(Math.abs(value), 8, list);
	}
	private static void toListDifference(int value, List<Byte> list) {
		//System.out.println("Difference: " + value);
		WriteBit(0, list);
	    WriteBit(0, list);
	    
	    int max = 0;
        int bits = 0;
        
        if(value >= -2 && value <= 2) {
            WriteBit(0, list);
            WriteBit(0, list);
            bits = 2;
            max = 2;
        }
        else if(value >= -6 && value <= 6) {
            WriteBit(1, list);
            WriteBit(0, list);
            bits = 3;
            max = 6;
        }
        else if(value >= -14 && value <= 14) {
            WriteBit(0, list);
            WriteBit(1, list);
            bits = 4;
            max = 14;
        }
        else if(value >= -30 && value <= 30) {
            WriteBit(1, list);
            WriteBit(1, list);
            bits = 5;
            max = 30;
        }
        int min = max / 2;

        int index = 0;
        for(int i = -max; i <= -min; i++) {
            if(i == value) {
            	WriteNumber(index, bits, list);
            	//System.out.println(getBitString(index, bits));
                return;
            }
            index++;
        }
        for(int i = min; i <= max; i++) {
            if(i == value) {
                WriteNumber(index, bits, list);
                //System.out.println(getBitString(index, bits));
                return;
            }
            index++;
        }
	}
	
	/*
	 *  DECOMPRESSION
	 */
	public static String decompressString(List<Byte> list) {
		int[] values = decompress(list);
		String text = "";
		for(int val : values) text += (char)val;
		return text; 
	}
	public static int[] decompress(List<Byte> list) {
		System.out.println("DECOMPRESSING");

		List<Integer> values = new ArrayList<>();
		bits = 0;
		bitCount = 0;
		currentByte = 0;
		
		// 1. first value
		values.add(ReadNumber(list, 8));
		
		// 2. other
		int index = ReadNumber(list, 2);
		while(index != 3) { // if bits 00 end
			if(index == 0) { 
				// 00
				fromListDifference(list, values);
			}
			else if(index == 1) { 
				// 01
				fromListRepetition(list, values);
			} else { 
				// 10
				fromListAbsolute(list, values);
			}
			//System.out.println(getBitString(index, 2));
			//System.out.println(values.toString() + " ("+bitCount+"b)");

			index = ReadNumber(list, 2);
		}
		
		//System.out.println(values.toString());
		int[] arr = new int[values.size()];
		for(int i = 0; i < values.size(); i++) arr[i] = values.get(i);
        return values.size() > 0 ? arr : null;
	}
	
	private static void fromListRepetition(List<Byte> list, List<Integer> values) {
		int repetition = ReadNumber(list, 3);
		int reapeatValue = values.get(values.size()-1);
		for(int i = 0; i < repetition + 1; i++) values.add(reapeatValue);
	}
	private static void fromListAbsolute(List<Byte> list, List<Integer> values) {
		int prefix = ReadBit(list);
		int value = prefix == 1 ? -ReadNumber(list, 8) : ReadNumber(list, 8);
		values.add(values.get(values.size()-1) + value);
	}
	private static void fromListDifference(List<Byte> list, List<Integer> values) {
	    int max = 0;
        int bits = ReadNumber(list, 2) + 2;
		int reapeatValue = values.get(values.size()-1);
        
        if(bits == 2) {
            max = 2;
        }
        else if(bits == 3) {
            max = 6;
        }
        else if(bits == 4) {
            max = 14;
        }
        else if(bits == 5) {
            max = 30;
        }
        
        int min = max / 2;
        //System.out.println(bits + " " + max + " " + min);
        int index = 0, numberIndex = ReadNumber(list, bits);
        for(int i = -max; i <= max; i++) {
        	if(i > -min && i < min) continue;
        	//System.out.println(i + " " + getBitString(numberIndex, bits));
        	if(index == numberIndex) {
        		values.add(reapeatValue + i);
        		return;
        	}
        	index++;
        }
	}
	
	
	/*
	 * BIT HANDELING FUNCTIONS
	 */
	private static void WriteBit(int bit, List<Byte> list)
    {
		if(bit == 1) { // 1
            byte b = list.get(list.size() - 1);
            b |= 1 << bits;
            list.set(list.size() - 1, b);
        }
        else { // 0
            byte b = list.get(list.size() - 1);
            b &= ~(1 << bits);
            list.set(list.size() - 1, b);
        }

        bits++;
        bitCount++;
        if (bits == 8)
        {
            list.add((byte)0);
            bits = 0;
        }
    }
	private static void WriteNumber(int value, int bits, List<Byte> list)
    {
        for(int i = 0; i < bits; i++)
        {
            WriteBit(GetBitAtPosition(i, value), list);
        }
    }	
	private static int ReadBit(List<Byte> list)
    {
        int v = (int)GetBitAtPosition(bits, list.get(currentByte));
        bits++;
        bitCount++;
        if (bits == 8)
        {
        	//System.out.println(getBitString(list.get(currentByte), 8) + " ("+currentByte+")");
            currentByte++;
            bits = 0;
        }
        return v;
    }
	private static int ReadNumber(List<Byte> list, int bitSize)
    {
        int value = 0, bit;
        for (int i = 0; i < bitSize; i++)
        {
            bit = ReadBit(list);
            if (bit == 1) value |= (int)(1 << i);  // 1
            else value &= (int)~(1 << i); // 0
        }
        //Console.WriteLine(">" + v + "<");
        return value;
    }
	
	private static int GetBitAtPosition(int position, int value)
    {
        return ((1 << position) & value) >> position;
    }
	
	
	/*
	 * DEBUG
	 */
	private static void out(int[] values) {
		String text = "[ ";
		for(int val : values) text += val + " ";
		System.out.println(text + "]"); 
	}
	private static void out(List<Byte> list) {
		for(Byte b : list) System.out.println(getBitString(b,8));
	}
	private static String getBitString(int value, int bit) {
		return String.format("%"+bit+"s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
	}
}
