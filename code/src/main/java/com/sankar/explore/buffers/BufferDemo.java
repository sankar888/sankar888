package com.sankar.explore.buffers;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.util.regex.Pattern;

public class BufferDemo {

	//Direct Buffers Demo Starts
	private static void demoDirectVsNormalBuffer() {
		printMemoryUsage(0);
		ByteBuffer.allocate(1024); //used jdk heap memory
		printMemoryUsage(1);
		ByteBuffer.allocateDirect(1024); //uses native os memory
		printMemoryUsage(2); //should be same as previous invocation, since no jdk heap memory is used
	}

	private static void printMemoryUsage(int id) {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long used = total - free;
		System.out.printf("%d: Total Memory : %d KB, Free Memory : %d KB, Used Memory %d KB \n", id, (total/1024), (free/1024), (used/1024));
	}
	//Direct Buffers Demo Starts

	//Buffer Usage demo : to read and write from terminal
	private static void demoBasicBufferUsage() {
		//allocate 1024 character buffer
		//initially, position = 0, limit = capacity = 1024
		CharBuffer buffer = CharBuffer.allocate(1024);
		System.out.println("Write something and see it being echoed back!");
		try (
				InputStreamReader reader = new InputStreamReader(System.in, "UTF-8");
				OutputStreamWriter writer = new OutputStreamWriter(System.out, "UTF-8");
		) {
			boolean flag = true;
			while (flag) {
				int chRead = reader.read(buffer); //for simplicity sake we consider only the first 1024 characters from the standard input
				/*
				 * after data is read the position will be moved by the no of characters read, limit will unchanged
				 * flip is necessary to get data from the buffer after a write
				 * flip will mark the range where data is available in buffer
				 * flip will mark position to 0, and limit to the index up where data is available
				 * so the writer will read data from index (position to limit)
				 */
				buffer.flip();

				// !pitfall
				//the below print statement will not work (coz buffer will produce a array with 1024 characters, we have no way to maek the data region
				//the buffer is not actually cleared everytime, it just have position and limit markers to denote the data space,
				System.out.print(buffer.array());

				//proper solution,
				//if buffer is used to transsfer data only use reader and writer which both supports buffer, else you have to manually mark the data region in array based buffers
				writer.append(buffer);
				writer.flush();
				buffer.clear(); //clear doesnot clears the buffer, it just reset the position and limit pointers
			}
		} catch (IOException e) {
			System.out.println("Error : "+e.getMessage());
			System.exit(1);
		}
		System.exit(0);
	}
	//data transfer using buffer ends

	//buffer and its positional methods
	private static void demoBufferPositionalMethods() {
		CharBuffer buffer = CharBuffer.allocate(10);
		CharBuffer target = CharBuffer.allocate(10);
		try {
			printCharBuffer(buffer, "initial state");
			buffer.append("hello"); //will write to buffer, this will change the position according to the length of the data
			printCharBuffer(buffer, "After putting 'hello'");
			buffer.flip(); //will mark the position of the data (data is always between pointers position to limit), flip is needed if we need to read from buffer
			printCharBuffer(buffer, "After calling flip()");
			buffer.read(target); //read will change the position index to limit
			printCharBuffer(buffer, "After reading from buffer");
			buffer.rewind(); //rewind is used to reread from the buffer, it sets the position to 0
			printCharBuffer(buffer, "After calling rewind()");
			buffer.clear(); //clear does not clears the contents, it sets the positional pointers to initial state
			printCharBuffer(buffer, "After calling clear()");
			buffer.append("abc");
			printCharBuffer(buffer, "After putting 'abc'");
			buffer.mark(); //mark will mark the current position
			buffer.append("de");
			printCharBuffer(buffer, "After putting 'de'");
			buffer.reset(); //reset will change the position pointer to previously marked position
			printCharBuffer(buffer, "After calling reset()");
		} catch (IOException e) {
			System.out.println("Error : "+e.getMessage());
			System.exit(1);
		}
	}

	private static void printCharBuffer(CharBuffer buffer, String description) {
		int position = buffer.position();
		int limit = buffer.limit();
		int capacity = buffer.capacity();
		System.out.printf("%25s ------> position: %s, limit: %s, capacity: %d, Always (position <= limit <= capacity), contents: %s \n",description, position, limit, capacity, String.valueOf(buffer.array()));
	}

	public static void main(String[] args) {
		//demoDirectVsNormalBuffer();
		//demoBasicBufferUsage();
		demoBufferPositionalMethods();
	}
}
