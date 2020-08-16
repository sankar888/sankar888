---
layout: post
title: Java NIO Buffers
published: true
date: 2020-08-16 18:35:00 +0530
categories:
 - java
 - input / output
 - buffers
 - io
tag:
 - tech 
---
### Java [Buffer](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/nio/Buffer.html) and its usage

##### What is buffer ? 
- A container for data of a specific primitive type.
- A buffer is a linear, finite sequence of elements of a specific 
primitive type.  Aside from its content, the essential properties of a
buffer are its capacity, limit, and position
- Java `java.nio.Buffer` is introduced in java 1.4 as part of new I/O package

##### Subclasses of Variants of buffer ?
- `java.nio.Buffer` itself is an abstract class, We have seven variants of buffer one for each primitive type (except boolean)
    - ByteBuffer
    - CharBuffer
    - DoubleBuffer
    - FloatBuffer
    - IntBuffer
    - LongBuffer
    - ShortBuffer
    
##### Buffer seems pretty much like array why do we need buffer and where to use it ?
| Arrays | Buffers |
| ------ | ------- |
| Arrays are containers of fixed size, which can hold any type of homogeneous data not just primitive types | Buffers can hold only primitive data type |
| Arrays are general purpose data structure  | Buffers are introduced specially for the use with `java.nio.Channel`, Almost all java Channels use Buffers to read and write data |
| - | Buffers internally uses array for its implementation |
| - | Buffers has the concept of capacity (similar to array size), position (current position of the index), and limit (the index up to where data is present) |
| - | Buffers has methods to produce arrays representing its current state |
| - | Buffers has extra methods such as `flip(), clear() compact()` which is closely tied to the concept of 'position, limit, capacity' |
| - | The memory of Buffers can be mapped to jdk heap or directly in os memory | 

- So to summarize, buffers are specially introduced to read and write data to and from `java.nio.Channel`. It has inbuilt provision to mark which of its indexes hold actual data
As the name implies use Buffers as a buffering space for data while data processing and use array to store any other homogeneous data.    

#### Example Code Snippets
##### Direct vs Non Direct Buffers
- A byte buffer is either direct or non-direct. Given a direct byte buffer, the Java virtual machine will make a best effort to perform native I/O operations directly upon it. That is, it will attempt to avoid copying the buffer's content to (or from) an intermediate buffer before (or after) each invocation of one of the underlying operating system's native I/O operations.
- A direct byte buffer may be created by invoking the allocateDirect factory method of this class. The buffers returned by this method typically have somewhat higher allocation and deallocation costs than non-direct buffers. The contents of direct buffers may reside outside of the normal garbage-collected heap, and so their impact upon the memory footprint of an application might not be obvious. It is therefore recommended that direct buffers be allocated primarily for large, long-lived buffers that are subject to the underlying system's native I/O operations. In general it is best to allocate direct buffers only when they yield a measurable gain in program performance.
The following program illustrates the memory allocation of direct buffers 
```java
public class BufferDemo {

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

	public static void main(String[] args) {
		demoDirectVsNormalBuffer();
	}
}
```
```text
Output:
0: Total Memory : 15872 KB, Free Memory : 14393 KB, Used Memory 1478 KB 
1: Total Memory : 15872 KB, Free Memory : 13598 KB, Used Memory 2273 KB 
2: Total Memory : 15872 KB, Free Memory : 13598 KB, Used Memory 2273 KB
```                    
In the above example see how output 1 and 2 shows same record, ie `ByteBuffer.allocateDirect(1024);` has not used any java heap memory.
Refer references 1 and 2 to learn more about direct os memory usage  

##### Various Positional pointers and state changing methods of buffer
- A buffer's capacity is the number of elements it contains. The capacity of a buffer is never negative and never changes.
- A buffer's limit is the index of the first element that should not be read or written. A buffer's limit is never negative and is never greater than its capacity.
- A buffer's position is the index of the next element to be read or written. A buffer's position is never negative and is never greater than its limit.
- Always position <= limit <= capacity
The following program demonstrated the various methods of buffer   
```java
public class BufferDemo {
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
		demoBufferPositionalMethods();
	}
}
```
```text
Output of the above code:
            initial state ------> position: 0, limit: 10, capacity: 10, Always (position <= limit <= capacity), contents:            
    After putting 'hello' ------> position: 5, limit: 10, capacity: 10, Always (position <= limit <= capacity), contents: hello      
     After calling flip() ------> position: 0, limit: 5, capacity: 10, Always (position <= limit <= capacity), contents: hello      
After reading from buffer ------> position: 5, limit: 5, capacity: 10, Always (position <= limit <= capacity), contents: hello      
   After calling rewind() ------> position: 0, limit: 5, capacity: 10, Always (position <= limit <= capacity), contents: hello      
    After calling clear() ------> position: 0, limit: 10, capacity: 10, Always (position <= limit <= capacity), contents: hello      
      After putting 'abc' ------> position: 3, limit: 10, capacity: 10, Always (position <= limit <= capacity), contents: abclo      
       After putting 'de' ------> position: 5, limit: 10, capacity: 10, Always (position <= limit <= capacity), contents: abcde      
    After calling reset() ------> position: 3, limit: 10, capacity: 10, Always (position <= limit <= capacity), contents: abcde
```

##### Simple Usage of Buffers in data transfer
The following code will read from standard input (terminal) and echos the same to the standard out (terminal)
The code demonstrated how the conversion of buffer to array without considering its positional pointers will cause issues
   
```java
public class BufferDemo {
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
				//System.out.print(buffer.array());

				//proper solution,
				//if buffer is used to transsfer data only use reader and writer which both supports buffer, else you have to manually mark the data region in array based buffers
				writer.append(buffer);
				writer.flush();
				buffer.clear(); //clear does not clears the buffer, it just reset the position and limit pointers
			}
		} catch (IOException e) {
			System.out.println("Error : "+e.getMessage());
			System.exit(1);
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		demoBasicBufferUsage();
	}
}
```

#### Resources
1. [Trouble shooting Problems with native heap memory apps](https://dzone.com/articles/troubleshooting-problems-with-native-off-heap-memo)
2. [When does direct buffer released?](https://stackoverflow.com/questions/36077641/java-when-does-direct-buffer-released)
3. [Buffers java doc](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/nio/Buffer.html)
4. [Source code of the snippets used](https://sankar888.githb.io/code/com/sankar/explore/buffers/BufferDemo.java)



         
    