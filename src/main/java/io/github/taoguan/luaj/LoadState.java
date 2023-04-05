package io.github.taoguan.luaj;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
* Class to undump compiled lua bytecode into a {@link io.github.taoguan.luaj.Prototype} instances.
* <p>
* The {@link LoadState} class provides the default {@link io.github.taoguan.luaj.Globals.Undumper}
* which is used to undump a string of bytes that represent a lua binary file
* using either the C-based lua compiler, or luaj's 
* {@link io.github.taoguan.luaj.compiler.LuaC} compiler.
* <p>
* The canonical method to load and execute code is done 
* indirectly using the Globals:
* <pre> {@code
* Globals globals = JsePlatform.standardGlobals();
* LuaValue chunk = globasl.load("print('hello, world')", "main.lua");
* chunk.call();
* } </pre>
* This should work regardless of which {@link io.github.taoguan.luaj.Globals.Compiler} or {@link io.github.taoguan.luaj.Globals.Undumper}
* have been installed.
* <p>
* By default, when using {@link io.github.taoguan.luaj.lib.jse.JsePlatform} or 
* to construct globals, the {@link LoadState} default undumper is installed
* as the default {@link io.github.taoguan.luaj.Globals.Undumper}.
* <p>
* 
* A lua binary file is created via the {@link io.github.taoguan.luaj.compiler.DumpState} class
:
* <pre> {@code
* Globals globals = JsePlatform.standardGlobals();
* Prototype p = globals.compilePrototype(new StringReader("print('hello, world')"), "main.lua");
* ByteArrayOutputStream o = new ByteArrayOutputStream();
* io.github.taoguan.luaj.compiler.DumpState.dump(p, o, false);
* byte[] lua_binary_file_bytes = o.toByteArray();
* } </pre>
* 
* The {@link LoadState}'s default undumper {@link #instance} 
* may be used directly to undump these bytes:
* <pre> {@code
* Prototypep = LoadState.instance.undump(new ByteArrayInputStream(lua_binary_file_bytes), "main.lua");
* LuaClosure c = new LuaClosure(p, globals);
* c.call();
* } </pre>
* 
* 
* More commonly, the {@link io.github.taoguan.luaj.Globals.Undumper} may be used to undump them:
* <pre> {@code
* Prototype p = globals.loadPrototype(new ByteArrayInputStream(lua_binary_file_bytes), "main.lua", "b");
* LuaClosure c = new LuaClosure(p, globals);
* c.call();
* } </pre>
* 
* @see io.github.taoguan.luaj.Globals.Compiler
* @see io.github.taoguan.luaj.Globals.Undumper
* @see LuaClosure
* @see LuaFunction
* @see io.github.taoguan.luaj.compiler.LuaC
* @see io.github.taoguan.luaj.luajc.LuaJC
* @see io.github.taoguan.luaj.Globals#compiler
*/
public class LoadState {

	/** Shared instance of Globals.Undumper to use loading prototypes from binary lua files */
	public static final io.github.taoguan.luaj.Globals.Undumper instance = new GlobalsUndumper();
	
	/** format corresponding to non-number-patched lua, all numbers are floats or doubles */
	public static final int NUMBER_FORMAT_FLOATS_OR_DOUBLES    = 0;

	/** format corresponding to non-number-patched lua, all numbers are ints */
	public static final int NUMBER_FORMAT_INTS_ONLY            = 1;
	
	/** format corresponding to number-patched lua, all numbers are 32-bit (4 byte) ints */
	public static final int NUMBER_FORMAT_NUM_PATCH_INT32      = 4;
	
	// type constants	
	public static final int LUA_TINT            = (-2);
	public static final int LUA_TNONE			= (-1);
	public static final int LUA_TNIL			= 0;
	public static final int LUA_TBOOLEAN		= 1;
	public static final int LUA_TLIGHTUSERDATA	= 2;
	public static final int LUA_TNUMBER			= 3;
	public static final int LUA_TSTRING			= 4;
	public static final int LUA_TTABLE			= 5;
	public static final int LUA_TFUNCTION		= 6;
	public static final int LUA_TUSERDATA		= 7;
	public static final int LUA_TTHREAD			= 8;
	public static final int LUA_TVALUE          = 9;
	
	/** The character encoding to use for file encoding.  Null means the default encoding */
	public static String encoding = null;
	
	/** Signature byte indicating the file is a compiled binary chunk */
	public static final byte[] LUA_SIGNATURE	= { '\033', 'L', 'u', 'a' };

	/** Data to catch conversion errors */
	public static final byte[] LUAC_TAIL = { (byte) 0x19, (byte) 0x93, '\r', '\n', (byte) 0x1a, '\n', };
	

	/** Name for compiled chunks */
	public static final String SOURCE_BINARY_STRING = "binary string";


	/** for header of binary files -- this is Lua 5.3 */
	public static final int LUAC_VERSION		= 0x53;

	/** for header of binary files -- this is the official format */
	public static final int LUAC_FORMAT		= 0;

	/** size of header of binary files */
	public static final int LUAC_HEADERSIZE		= 12;

	// values read from the header
	private int     luacVersion;
	private int     luacFormat;
	private boolean luacLittleEndian;
	private int     luacSizeofInt;
	private int     luacSizeofSizeT;
	private int     luacSizeofInstruction;
	private int     luacSizeofLuaNumber;
	private int 	luacNumberFormat;

	/** input stream from which we are loading */
	public final DataInputStream is;

	/** Name of what is being loaded? */
	String name;

	private static final io.github.taoguan.luaj.LuaValue[]     NOVALUES    = {};
	private static final io.github.taoguan.luaj.Prototype[] NOPROTOS    = {};
	private static final io.github.taoguan.luaj.LocVars[]   NOLOCVARS   = {};
	private static final io.github.taoguan.luaj.LuaString[]  NOSTRVALUES = {};
	private static final Upvaldesc[]  NOUPVALDESCS = {};
	private static final int[]       NOINTS      = {};
	
	/** Read buffer */
	private byte[] buf = new byte[512];

	/** Install this class as the standard Globals.Undumper for the supplied Globals */
	public static void install(io.github.taoguan.luaj.Globals globals) {
		globals.undumper = instance;
	}
	
	/** Load a 4-byte int value from the input stream
	 * @return the int value laoded.  
	 **/
	int loadInt() throws IOException {
		is.readFully(buf,0,4);
		return luacLittleEndian? 
				(buf[3] << 24) | ((0xff & buf[2]) << 16) | ((0xff & buf[1]) << 8) | (0xff & buf[0]):
				(buf[0] << 24) | ((0xff & buf[1]) << 16) | ((0xff & buf[2]) << 8) | (0xff & buf[3]);
	}
	
	/** Load an array of int values from the input stream
	 * @return the array of int values laoded.  
	 **/
	int[] loadIntArray() throws IOException {
		int n = loadInt();
		if ( n == 0 )
			return NOINTS;
		
		// read all data at once
		int m = n << 2;
		if ( buf.length < m )
			buf = new byte[m];
		is.readFully(buf,0,m);
		int[] array = new int[n];
		for ( int i=0, j=0; i<n; ++i, j+=4 )
			array[i] = luacLittleEndian? 
					(buf[j+3] << 24) | ((0xff & buf[j+2]) << 16) | ((0xff & buf[j+1]) << 8) | (0xff & buf[j+0]):
					(buf[j+0] << 24) | ((0xff & buf[j+1]) << 16) | ((0xff & buf[j+2]) << 8) | (0xff & buf[j+3]);

		return array;
	}
	
	/** Load a long  value from the input stream
	 * @return the long value laoded.  
	 **/
	long loadInt64() throws IOException {
		int a,b;
		if ( this.luacLittleEndian ) {
			a = loadInt();
			b = loadInt();
		} else {
			b = loadInt();
			a = loadInt();
		}
		return (((long)b)<<32) | (((long)a)&0xffffffffL);
	}

	/** Load a lua strin gvalue from the input stream
	 * @return the {@link io.github.taoguan.luaj.LuaString} value laoded.
	 **/
	io.github.taoguan.luaj.LuaString loadString() throws IOException {
		int size = this.luacSizeofSizeT == 8? (int) loadInt64(): loadInt();
		if ( size == 0 )
			return null;
		byte[] bytes = new byte[size];
		is.readFully( bytes, 0, size );
		return io.github.taoguan.luaj.LuaString.valueUsing( bytes, 0, bytes.length - 1 );
	}
	
	/**
	 * Convert bits in a long value to a {@link io.github.taoguan.luaj.LuaValue}.
	 * @param bits long value containing the bits
	 * @return {@link LuaInteger} or {@link LuaDouble} whose value corresponds to the bits provided.
	 */
	public static io.github.taoguan.luaj.LuaValue longBitsToLuaNumber(long bits ) {
		if ( ( bits & ( ( 1L << 63 ) - 1 ) ) == 0L ) {
			return io.github.taoguan.luaj.LuaValue.ZERO;
		}
		
		int e = (int)((bits >> 52) & 0x7ffL) - 1023;
		
		if ( e >= 0 && e < 31 ) {
			long f = bits & 0xFFFFFFFFFFFFFL;
			int shift = 52 - e;
			long intPrecMask = ( 1L << shift ) - 1;
			if ( ( f & intPrecMask ) == 0 ) {
				int intValue = (int)( f >> shift ) | ( 1 << e );
				return LuaInteger.valueOf( ( ( bits >> 63 ) != 0 ) ? -intValue : intValue );
			}
		}
		
		return io.github.taoguan.luaj.LuaValue.valueOf( Double.longBitsToDouble(bits) );
	}
	
	/** 
	 * Load a number from a binary chunk
	 * @return the {@link io.github.taoguan.luaj.LuaValue} loaded
	 * @throws IOException if an i/o exception occurs
	 */
	io.github.taoguan.luaj.LuaValue loadNumber() throws IOException {
		if ( luacNumberFormat == NUMBER_FORMAT_INTS_ONLY ) {
			return LuaInteger.valueOf( loadInt() );
		} else {
			return longBitsToLuaNumber( loadInt64() );
		}
	}

	/**
	 * Load a list of constants from a binary chunk
	 * @param f the function prototype
	 * @throws IOException if an i/o exception occurs
	 */
	void loadConstants(io.github.taoguan.luaj.Prototype f) throws IOException {
		int n = loadInt();
		io.github.taoguan.luaj.LuaValue[] values = n>0? new io.github.taoguan.luaj.LuaValue[n]: NOVALUES;
		for ( int i=0; i<n; i++ ) {
			switch ( is.readByte() ) {
			case LUA_TNIL:
				values[i] = io.github.taoguan.luaj.LuaValue.NIL;
				break;
			case LUA_TBOOLEAN:
				values[i] = (0 != is.readUnsignedByte()? io.github.taoguan.luaj.LuaValue.TRUE: io.github.taoguan.luaj.LuaValue.FALSE);
				break;
			case LUA_TINT:
				values[i] = LuaInteger.valueOf( loadInt() );
				break;
			case LUA_TNUMBER:
				values[i] = loadNumber();
				break;
			case LUA_TSTRING:
				values[i] = loadString();
				break;
			default:
				throw new IllegalStateException("bad constant");
			}
		}
		f.k = values;
		
		n = loadInt();
		io.github.taoguan.luaj.Prototype[] protos = n>0? new io.github.taoguan.luaj.Prototype[n]: NOPROTOS;
		for ( int i=0; i<n; i++ )
			protos[i] = loadFunction(f.source);
		f.p = protos;
	}


	void loadUpvalues(io.github.taoguan.luaj.Prototype f) throws IOException {
		int n = loadInt();
		f.upvalues = n>0? new Upvaldesc[n]: NOUPVALDESCS;
		for (int i=0; i<n; i++) {
			boolean instack = is.readByte() != 0;
			int idx = ((int) is.readByte()) & 0xff;
			f.upvalues[i] = new Upvaldesc(null, instack, idx);
		}
	}

	/**
	 * Load the debug info for a function prototype
	 * @param f the function Prototype
	 * @throws IOException if there is an i/o exception
	 */
	void loadDebug( io.github.taoguan.luaj.Prototype f ) throws IOException {
		f.source = loadString();
		f.lineinfo = loadIntArray();
		int n = loadInt();
		f.locvars = n>0? new io.github.taoguan.luaj.LocVars[n]: NOLOCVARS;
		for ( int i=0; i<n; i++ ) {
			io.github.taoguan.luaj.LuaString varname = loadString();
			int startpc = loadInt();
			int endpc = loadInt();
			f.locvars[i] = new io.github.taoguan.luaj.LocVars(varname, startpc, endpc);
		}
		
		n = loadInt();
		for ( int i=0; i<n; i++ )
			f.upvalues[i].name = loadString();
	}

	/** 
	 * Load a function prototype from the input stream
	 * @param p name of the source
	 * @return {@link io.github.taoguan.luaj.Prototype} instance that was loaded
	 * @throws IOException
	 */
	public io.github.taoguan.luaj.Prototype loadFunction(io.github.taoguan.luaj.LuaString p) throws IOException {
		io.github.taoguan.luaj.Prototype f = new io.github.taoguan.luaj.Prototype();
////		this.L.push(f);
//		f.source = loadString();
//		if ( f.source == null )
//			f.source = p;
		f.linedefined = loadInt();
		f.lastlinedefined = loadInt();
		f.numparams = is.readUnsignedByte();
		f.is_vararg = is.readUnsignedByte();
		f.maxstacksize = is.readUnsignedByte();
		f.code = loadIntArray();
		loadConstants(f);
		loadUpvalues(f);
		loadDebug(f);
		
		// TODO: add check here, for debugging purposes, I believe
		// see ldebug.c
//		 IF (!luaG_checkcode(f), "bad code");
		
//		 this.L.pop();
		 return f;
	}

	/**
	 * Load the lua chunk header values. 
	 * @throws IOException if an i/o exception occurs. 
	 */
	public void loadHeader() throws IOException {
		luacVersion = is.readByte();
		luacFormat = is.readByte();
		luacLittleEndian = (0 != is.readByte());
		luacSizeofInt = is.readByte();
		luacSizeofSizeT = is.readByte();
		luacSizeofInstruction = is.readByte();
		luacSizeofLuaNumber = is.readByte();
		luacNumberFormat = is.readByte();
		for (int i=0; i < LUAC_TAIL.length; ++i)
			if (is.readByte() != LUAC_TAIL[i])
				throw new LuaError("Unexpeted byte in luac tail of header, index="+i);
	}

	/**
	 * Load input stream as a lua binary chunk if the first 4 bytes are the lua binary signature.
	 * @param stream InputStream to read, after having read the first byte already
	 * @param chunkname Name to apply to the loaded chunk
	 * @return {@link io.github.taoguan.luaj.Prototype} that was loaded, or null if the first 4 bytes were not the lua signature.
	 * @throws IOException if an IOException occurs
	 */
	public static io.github.taoguan.luaj.Prototype undump(InputStream stream, String chunkname) throws IOException {
		// check rest of signature
		if ( stream.read() != LUA_SIGNATURE[0] 
		   || stream.read() != LUA_SIGNATURE[1]
	       || stream.read() != LUA_SIGNATURE[2]
		   || stream.read() != LUA_SIGNATURE[3] )
			return null;
		
		// load file as a compiled chunk
		String sname = getSourceName(chunkname);
		LoadState s = new LoadState( stream, sname );
		s.loadHeader();

		// check format
		switch ( s.luacNumberFormat ) {
		case NUMBER_FORMAT_FLOATS_OR_DOUBLES:
		case NUMBER_FORMAT_INTS_ONLY:
		case NUMBER_FORMAT_NUM_PATCH_INT32:
			break;
		default:
			throw new LuaError("unsupported int size");
		}
		return s.loadFunction( io.github.taoguan.luaj.LuaString.valueOf(sname) );
	}
	
	/**
	 * Construct a source name from a supplied chunk name
	 * @param name String name that appears in the chunk
	 * @return source file name
	 */
    public static String getSourceName(String name) {
        String sname = name;
        if ( name.startsWith("@") || name.startsWith("=") )
			sname = name.substring(1);
		else if ( name.startsWith("\033") )
			sname = SOURCE_BINARY_STRING;
        return sname;
    }

	/** Private constructor for create a load state */
	private LoadState(InputStream stream, String name ) {
		this.name = name;
		this.is = new DataInputStream( stream );
	}
	
	private static final class GlobalsUndumper implements io.github.taoguan.luaj.Globals.Undumper {
		public io.github.taoguan.luaj.Prototype undump(InputStream stream, String chunkname)
				throws IOException {
			return LoadState.undump(stream,  chunkname);
		}
	}
}
