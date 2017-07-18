package com.evilfrogindustries.networkingtutorial;

public class Notes {
}

/*
--Will write notes on the basic methods
for each type of stream if requested.

*********************** BASIC DEFINITIONS ***************************

- (Input/OutputStream) + Filter stream  + buffered stream -- Used for working with raw bytes.
- (reader / writer) + (reader/ writer filter) + reader / writer buffer -- Used for working with encoded characters.

--Two types of ways to modify basic streams of bytes.
***filter stream (works with the raw data, IE: compressing)
***reader and writer (works with characters in certain encodings)


Example Input Stream Setup.
InputStream - buffer - filter stream / reader

***EXAMPLE - TelnetInputStream (InputStream) - BufferedInputStream (buffer) - CipherInputStream (filter stream) - GzipInputStream(filter stream) - InputStreamReader(reader, reads text)


--Special Notes on DataOutputStream
DataOutputStream.writeBytes - writes only the least significant byte of each char, useful for protocols that need ascii which is 7 bits

DataOutputStream.writeUTF - includes length of string, most useful for writing out words
- Encodes in variant of utf-8 Unicode, this is not useful for non java software, is it is java specific.
A better way is to write teh data using a OutPutStream with the needed encoding, and read it the same way


-----General Setup--------

****************filters/ streams -- ******************************************************************************************
**OutputStream - basic abstract class for streams of bytes
**InputStream - basic abstract class for streams of bytes

**PrintStream - basic output stream filter to output the default encoding characters of the system.
Suffers from system dependent new line characters, a lack of exception handling, and only using the default character encoding.

**DataInputStream / DataOutputStream - read and write java primitive data types and strings in binary
-intended for sharing data between java programs, works for internet protocols in some cases.

**BufferedInputStream / BufferedOutputStream - buffer the raw byte streams.


****************************************************************************************************************************


****************readers / writers ---******************************************************************************************
**Reader - abstract class for reading characters
**Writer - abstract class for writing characters
- both convert bytes into a certain character encoding.
- have concrete classes, and filter readers and writers that perform the same function as filter streams

-Two most important concrete classes:
**InputStreamReader - reads an input stream in a certain encoding, has underlying InputStream
**OutputStreamWriter - writes to an output stream in a certain encoding, has an underlying OutputStream

--These classes act like a filter on top of input and output streams, converting the bytes to characters.

--To buffer a writer or reader, chain the underlying InputStream / OutputStream to a BufferedInputStream / BufferedOutputStream,
or simply use buffered reader or BufferedWriter to the reader or writer.

--Just like in streams, readers can have filters that modify the characters.

**FilterReader - Abstract class
**FilterWriter - Abstract Class
	**BufferedReader - buffers
	**BufferedWriter - buffers
	**printWriter - better version of PrintStream that handles text properly.

**-Some classes do not need underlying streams, they work with the object directly.
#File Reader
#file writer
#string reader
#string writer

***********************************************************************************************************************************

--Notes on Encoding Sets--

-It is almost always better to specify what encoding you are using, instead of relying on a system default.

-utf-16 is java default encoding, bytes and chars are NOT the same in this. As a result, we have writers to work with
character data.

--utf-16 - uses two bytes per character. Each byte is two hex digits.
	-big-endian - two zeros come first per character
		-- EX: 00 FF
	-little-endian - two zeros come after character byte
		-- EX: FF 00

--utf-8 / Latin -1  - uses one byte per character
	-EX: FF


   //Writes an Ascii character to an output stream using the write method of the FileOutputStream class
    public void writeAscii() {
        OutputStream out = null;
        try {
            out = new FileOutputStream("./src/data.txt");
            //Prints out capital A in ascii. Writing to a file
            //Converts the byte written to a ascii character.
            /*
            350 in binary is:
            1 01011110
            When casted to one byte, it is:
            01011110
            This is 94. 94 on the ascii chart is ^
            Therefore, the write method casts the int to a byte,
            and writes the corresponding Ascii code

            out.write(350);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {

            }
        }
    }

    //Does the same as the above method using the try-with block
    //Automatically closes the out stream.
    //Only used on objects that implement the "closable" interface.
    public void writeAsciiImproved() {
        try (OutputStream out = new FileOutputStream("./src/data.txt")) {
            out.write(350);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    //signed byte - -128 to 127
    //int i = b >= 0 ? b : 256 + b
    // i = (byte)b - can have values from -128 - 127
    //if you have -128,  you get 128. if you have -1, you get 255. So, in the positives you have 1 - 127, and you loop over with negatives.
    //127 in binary is everything filled up but the left most byte. It represents 127 in a signed byte value
    //128 is just the left most byte. It represents -128, and when you fill up spots to the right of it, it acts like adding to this negative number.

    /*
    int bytesRead = 0;
    int bytesToRead = 1024;
    byte[] input = new byte[bytesToRead];
    while(bytesRead < bytesToRead) {
        bytesRead += in.read(input, bytesRead, bytesToRead - bytesRead);
    }


    //Read method blocks execution until a single byte is availabe to be read. It reads a single byte and returns an int from 0 to 255. -1 if end of stream
    //wrte(byte[] input) attempts to fill the array with bytes read. It can fail if a connection is terminated, or it can only read in a amoutn
    //that is less than the size specified. The method returns an int to account for this, it returns how many bytes are read

    //Example fragment that takes care of the possibility that some bytes may not be available for reading
    public void dumbInput() throws IOException{
        InputStream in = new FileInputStream("Stuff.txt");
        byte[] input = new byte[1024];
        int bytesRead = in.read(input);
        //Tries to read in 1024 bytes, but may fail if not all bytes are available
    }

    //To ensure that all of the info you want is read, you make a while loop that doesn't stop till all of the info you need is read.
    //you have to specify where to start again, because if you just use the write method again, it starts from the beginning
    public void smartRead() throws IOException {
        InputStream in = new FileInputStream("Stuff.txt");
        int bytesRead = 0;
        int bytesToRead = 1024;
        byte[] input = new byte[1024];
        while(bytesRead < bytesToRead) {
            bytesRead += in.read(input, bytesRead, bytesToRead - bytesRead);
        }
    }

    /*
    The goal is to read all of the bytes, or 1024. Each time the read happens, it tests to see if all the bytes needed are read. If they are not,
    a new read occurs, starting at the position where the last bytes where read. It then attempts to read the remaining bytes, or, the amount to
    read minus the bytes read. This corrects the issue of a cpu reading all of the info that is currently available from the network(actually the data
    that is from the network currently store in the buffer), and not waiting for more info to come. The methods return a zero if the stream isn't closed,
    but no data was read, probably because the network is being slow. Instead of watiting for data to be availble, it returns a zero.
     */

    /*
    If a stream ends and there is still data in the buffer, the read method keeps going until the buffer is emptied. If the data never arrives or
    if the file reaches an end of stream before all of the data is supposed to be read, the above code will go on forever because
    it keep expecting more data. If a buffer is emptied and the read method is called again and the stream is closed, a -1 is returned.

    public void smartestRead() throws IOException {
        InputStream in = new FileInputStream("Stuff.txt");
        int bytesRead = 0;
        int bytesToRead = 1024;
        byte[] input = new byte[1024];
        while (bytesRead < bytesToRead) {
            int amountRead = in.read(input, bytesRead, bytesToRead - bytesRead);
            if (amountRead == -1) {
                break;
            }
            bytesRead += amountRead;
        }
    }

    //Note that this program blocks until all of the bytes that can be read are read.
    //bufferedoutputstream stores data written to a buffer until the buffer is full or the stream is flushed, then it writes to the output stream all at once
    //A single write of many things is smaller than multiple writes of a small thing
    //Writing one byte at a time over tcp is bad, because each datagram has a header that has overhead.
    //Always buffer network output, dumbass.

    //Input streams first try to read from the buffer, and only reads from stream if buffer runs out. It then reads as much as it can from the
    //stream to the buffer, and this is used for future reads. It is much faster to read all at once than one at a time.

    //buffered inputstream reads until all data is put in the byte array. not like the regular read methods. most read methods read only once.

    //filters.
    //Printstream println is platform dependent, and outputs a certain character dpeending on what OS is bieng used. this is bad for netowrk
    //Protocols becaues they need a certain character to specify a new line, and this is not achieved on different platforms.
    //Unix uses newline, mac uses carriage return, windows uses both, so you get a different newline character each time
    //Print stream also uses the default encoding of the platform it is running on, you may not want this as the server may not want this encoding
    //you can specify encoding with printwriter. printstream also doesn't throw expections, which is bad. you need to be able to deal with interrupts

    //Data streams

*/