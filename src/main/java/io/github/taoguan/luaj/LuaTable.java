package io.github.taoguan.luaj;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Subclass of {@link io.github.taoguan.luaj.LuaValue} for representing lua tables.
 * <p>
 * Almost all API's implemented in {@link LuaTable} are defined and documented in {@link io.github.taoguan.luaj.LuaValue}.
 * <p>
 * If a table is needed, the one of the type-checking functions can be used such as
 * {@link #istable()}, 
 * {@link #checktable()}, or
 * {@link #opttable(LuaTable)} 
 * <p>
 * The main table operations are defined on {@link io.github.taoguan.luaj.LuaValue}
 * for getting and setting values with and without metatag processing:
 * <ul>
 * <li>{@link #get(io.github.taoguan.luaj.LuaValue)}</li>
 * <li>{@link #set(io.github.taoguan.luaj.LuaValue, io.github.taoguan.luaj.LuaValue)}</li>
 * <li>{@link #rawget(io.github.taoguan.luaj.LuaValue)}</li>
 * <li>{@link #rawset(io.github.taoguan.luaj.LuaValue, io.github.taoguan.luaj.LuaValue)}</li>
 * <li>plus overloads such as {@link #get(String)}, {@link #get(int)}, and so on</li>  
 * </ul>
 * <p>
 * To iterate over key-value pairs from Java, use
 * <pre> {@code
 * LuaValue k = LuaValue.NIL;
 * while ( true ) {
 *    Varargs n = table.next(k);
 *    if ( (k = n.arg1()).isnil() )
 *       break;
 *    LuaValue v = n.arg(2)
 *    process( k, v )
 * }}</pre>
 * 
 * <p>
 * As with other types, {@link LuaTable} instances should be constructed via one of the table constructor 
 * methods on {@link io.github.taoguan.luaj.LuaValue}:
 * <ul>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tableOf()} empty table</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tableOf(int, int)} table with capacity</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#listOf(io.github.taoguan.luaj.LuaValue[])} initialize array part</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#listOf(io.github.taoguan.luaj.LuaValue[], io.github.taoguan.luaj.Varargs)} initialize array part</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tableOf(io.github.taoguan.luaj.LuaValue[])} initialize named hash part</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tableOf(io.github.taoguan.luaj.Varargs, int)} initialize named hash part</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tableOf(io.github.taoguan.luaj.LuaValue[], io.github.taoguan.luaj.LuaValue[])} initialize array and named parts</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tableOf(io.github.taoguan.luaj.LuaValue[], io.github.taoguan.luaj.LuaValue[], io.github.taoguan.luaj.Varargs)} initialize array and named parts</li>
 * </ul>
 * @see io.github.taoguan.luaj.LuaValue
 */
public class LuaTable extends io.github.taoguan.luaj.LuaValue implements Metatable {
	private static final int      MIN_HASH_CAPACITY = 2;
	private static final io.github.taoguan.luaj.LuaString N = valueOf("n");
	private static final Slot[] NOBUCKETS = {};
	/** the array values */
	protected io.github.taoguan.luaj.LuaValue[] array;
	
	/** the hash part */
	protected Slot[] hash;
	
	/** the number of hash entries */
	protected int hashEntries;
	
	/** metatable for this table, or null */
	protected Metatable m_metatable;
	
	/** Construct empty table */
	public LuaTable() {
		array = NOVALS;
		hash = NOBUCKETS;
	}
	
	/** 
	 * Construct table with preset capacity.
	 * @param narray capacity of array part
	 * @param nhash capacity of hash part
	 */
	public LuaTable(int narray, int nhash) {
		presize(narray, nhash);
	}

	/**
	 * Construct table with named and unnamed parts. 
	 * @param named Named elements in order {@code key-a, value-a, key-b, value-b, ... }
	 * @param unnamed Unnamed elements in order {@code value-1, value-2, ... } 
	 * @param lastarg Additional unnamed values beyond {@code unnamed.length}
	 */
	public LuaTable(io.github.taoguan.luaj.LuaValue[] named, io.github.taoguan.luaj.LuaValue[] unnamed, io.github.taoguan.luaj.Varargs lastarg) {
		int nn = (named!=null? named.length: 0);
		int nu = (unnamed!=null? unnamed.length: 0);
		int nl = (lastarg!=null? lastarg.narg(): 0);
		presize(nu+nl, nn>>1);
		for ( int i=0; i<nu; i++ )
			rawset(i+1,unnamed[i]);
		if ( lastarg != null )
			for ( int i=1,n=lastarg.narg(); i<=n; ++i )
				rawset(nu+i,lastarg.arg(i));
		for ( int i=0; i<nn; i+=2 )
			if (!named[i+1].isnil())
				rawset(named[i], named[i+1]);
	}

	/**
	 * Construct table of unnamed elements. 
	 * @param varargs Unnamed elements in order {@code value-1, value-2, ... } 
	 */
	public LuaTable(io.github.taoguan.luaj.Varargs varargs) {
		this(varargs,1);
	}

	/**
	 * Construct table of unnamed elements. 
	 * @param varargs Unnamed elements in order {@code value-1, value-2, ... }
	 * @param firstarg the index in varargs of the first argument to include in the table 
	 */
	public LuaTable(io.github.taoguan.luaj.Varargs varargs, int firstarg) {
		int nskip = firstarg-1;
		int n = Math.max(varargs.narg()-nskip,0);
		presize( n, 1 );
		set(N, valueOf(n));
		for ( int i=1; i<=n; i++ )
			set(i, varargs.arg(i+nskip));
	}
	
	public int type() {
		return io.github.taoguan.luaj.LuaValue.TTABLE;
	}

	public String typename() {
		return "table";
	}
	
	public boolean istable() { 
		return true; 
	}
	
	public LuaTable checktable() {
		return this;
	}

	public LuaTable opttable(LuaTable defval)  {
		return this;
	}
	
	public void presize( int narray ) {
		if ( narray > array.length )
			array = resize( array, 1 << log2(narray) );
	}

	public void presize(int narray, int nhash) {
		if ( nhash > 0 && nhash < MIN_HASH_CAPACITY )
			nhash = MIN_HASH_CAPACITY;
		// Size of both parts must be a power of two.
		array = (narray>0? new io.github.taoguan.luaj.LuaValue[1 << log2(narray)]: NOVALS);
		hash = (nhash>0? new Slot[1 << log2(nhash)]: NOBUCKETS);
		hashEntries = 0;
	}

	/** Resize the table */
	private static io.github.taoguan.luaj.LuaValue[] resize(io.github.taoguan.luaj.LuaValue[] old, int n ) {
		io.github.taoguan.luaj.LuaValue[] v = new io.github.taoguan.luaj.LuaValue[n];
		System.arraycopy(old, 0, v, 0, old.length);
		return v;
	}
	
	/** 
	 * Get the length of the array part of the table. 
	 * @return length of the array part, does not relate to count of objects in the table. 
	 */
	protected int getArrayLength() {
		return array.length;
	}

	/** 
	 * Get the length of the hash part of the table. 
	 * @return length of the hash part, does not relate to count of objects in the table. 
	 */
	protected int getHashLength() {
		return hash.length;
	}
	
	public io.github.taoguan.luaj.LuaValue getmetatable() {
		return ( m_metatable != null ) ? m_metatable.toLuaValue() : null;
	}
	
	public io.github.taoguan.luaj.LuaValue setmetatable(io.github.taoguan.luaj.LuaValue metatable) {
		m_metatable = metatableOf( metatable );
		return this;
	}
	
	public io.github.taoguan.luaj.LuaValue get(int key ) {
		io.github.taoguan.luaj.LuaValue v = rawget(key);
		return v.isnil() && m_metatable!=null? gettable(this,valueOf(key)): v;
	}
	
	public io.github.taoguan.luaj.LuaValue get(io.github.taoguan.luaj.LuaValue key ) {
		io.github.taoguan.luaj.LuaValue v = rawget(key);
		return v.isnil() && m_metatable!=null? gettable(this,key): v;
	}

	public io.github.taoguan.luaj.LuaValue rawget(int key ) {
		if ( key>0 && key<=array.length ) {
			io.github.taoguan.luaj.LuaValue v = m_metatable == null ? array[key-1] : m_metatable.arrayget(array, key-1);
			return v != null ? v : NIL;
		}
		return hashget( io.github.taoguan.luaj.LuaInteger.valueOf(key) );
	}

	public io.github.taoguan.luaj.LuaValue rawget(io.github.taoguan.luaj.LuaValue key ) {
		if ( key.isinttype() ) {
			int ikey = key.toint();
			if ( ikey>0 && ikey<=array.length ) {
				io.github.taoguan.luaj.LuaValue v = m_metatable == null
						? array[ikey-1] : m_metatable.arrayget(array, ikey-1);
				return v != null ? v : NIL;
			}
		}
		return hashget( key );
	}

	protected io.github.taoguan.luaj.LuaValue hashget(io.github.taoguan.luaj.LuaValue key) {
		if ( hashEntries > 0 ) {
			for ( Slot slot = hash[ hashSlot(key) ]; slot != null; slot = slot.rest() ) {
				StrongSlot foundSlot;
				if ( ( foundSlot = slot.find(key) ) != null ) {
					return foundSlot.value();
				}
			}
		}
		return NIL;
	}

	public void set( int key, io.github.taoguan.luaj.LuaValue value ) {
		if ( m_metatable==null || ! rawget(key).isnil() || ! settable(this, io.github.taoguan.luaj.LuaInteger.valueOf(key),value) )
			rawset(key, value);
	}

	/** caller must ensure key is not nil */
	public void set(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value ) {
		if (key == null || !key.isvalidkey() && !metatag(NEWINDEX).isfunction())
			throw new LuaError("value ('" + key + "') can not be used as a table index");
		if ( m_metatable==null || ! rawget(key).isnil() ||  ! settable(this,key,value) )
			rawset(key, value);
	}

	public void rawset( int key, io.github.taoguan.luaj.LuaValue value ) {
		if ( ! arrayset(key, value) )
			hashset( io.github.taoguan.luaj.LuaInteger.valueOf(key), value );
	}

	/** caller must ensure key is not nil */
	public void rawset(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value ) {
		if ( !key.isinttype() || !arrayset(key.toint(), value) )
			hashset( key, value );
	}

	/** Set an array element */
	private boolean arrayset( int key, io.github.taoguan.luaj.LuaValue value ) {
		if ( key>0 && key<=array.length ) {
			array[key - 1] = value.isnil() ? null :
				(m_metatable != null ? m_metatable.wrap(value) : value);
			return true;
		}
		return false;
	}

	/** Remove the element at a position in a list-table
	 *  
	 * @param pos the position to remove
	 * @return The removed item, or {@link #NONE} if not removed
	 */
	public io.github.taoguan.luaj.LuaValue remove(int pos) {
		int n = rawlen();
		if ( pos == 0 )
			pos = n;
		else if (pos > n)
			return NONE;
		io.github.taoguan.luaj.LuaValue v = rawget(pos);
		for (io.github.taoguan.luaj.LuaValue r = v; !r.isnil(); ) {
			r = rawget(pos+1);
			rawset(pos++, r);
		}
		return v.isnil()? NONE: v;
	}

	/** Insert an element at a position in a list-table
	 *  
	 * @param pos the position to remove
	 * @param value The value to insert
	 */
	public void insert(int pos, io.github.taoguan.luaj.LuaValue value) {
		if ( pos == 0 )
			pos = rawlen()+1;
		while ( ! value.isnil() ) {
			io.github.taoguan.luaj.LuaValue v = rawget( pos );
			rawset(pos++, value);
			value = v;
		}
	}

	/** Concatenate the contents of a table efficiently, using {@link io.github.taoguan.luaj.Buffer}
	 * 
	 * @param sep {@link io.github.taoguan.luaj.LuaString} separater to apply between elements
	 * @param i the first element index
	 * @param j the last element index, inclusive
	 * @return {@link io.github.taoguan.luaj.LuaString} value of the concatenation
	 */
	public io.github.taoguan.luaj.LuaValue concat(io.github.taoguan.luaj.LuaString sep, int i, int j) {
		io.github.taoguan.luaj.Buffer sb = new io.github.taoguan.luaj.Buffer();
		if ( i<=j ) {
			sb.append( get(i).checkstring() );
			while ( ++i<=j ) {
				sb.append( sep );
				sb.append( get(i).checkstring() );
			}
		}
		return sb.tostring();
	}

	public int length() {
		if (m_metatable != null) {
			io.github.taoguan.luaj.LuaValue len = len();
			if (!len.isint()) throw new LuaError("table length is not an integer: " + len);
			return len.toint();
		}
		return rawlen();
	}
	
	public io.github.taoguan.luaj.LuaValue len()  {
		final io.github.taoguan.luaj.LuaValue h = metatag(LEN);
		if (h.toboolean())
			return h.call(this);
		return io.github.taoguan.luaj.LuaInteger.valueOf(rawlen());
	}

	public int rawlen() { 
		int a = getArrayLength();
		int n = a+1,m=0;
		while ( !rawget(n).isnil() ) {
			m = n;
			n += a+getHashLength()+1;
		}
		while ( n > m+1 ) {
			int k = (n+m) / 2;
			if ( !rawget(k).isnil() )
				m = k;
			else
				n = k;
		}
		return m;
	}

	/**
	 * Get the next element after a particular key in the table 
	 * @return key,value or nil
	 */
	public io.github.taoguan.luaj.Varargs next(io.github.taoguan.luaj.LuaValue key ) {
		int i = 0;
		do {
			// find current key index
			if ( ! key.isnil() ) {
				if ( key.isinttype() ) {
					i = key.toint();
					if ( i>0 && i<=array.length ) {
						break;
					}
				}
				if ( hash.length == 0 )
					error( "invalid key to 'next' 1: " + key );
				i = hashSlot( key );
				boolean found = false;
				for ( Slot slot = hash[i]; slot != null; slot = slot.rest() ) {
					if ( found ) {
						StrongSlot nextEntry = slot.first();
						if ( nextEntry != null ) {
							return nextEntry.toVarargs();
						}
					} else if ( slot.keyeq( key ) ) {
						found = true;
					}
				}
				if ( !found ) {
					error( "invalid key to 'next' 2: " + key );
				}
				i += 1+array.length;
			}
		} while ( false );

		// check array part
		for ( ; i<array.length; ++i ) {
			if ( array[i] != null ) {
				io.github.taoguan.luaj.LuaValue value = m_metatable == null ? array[i] : m_metatable.arrayget(array, i);
				if (value != null) {
					return varargsOf(io.github.taoguan.luaj.LuaInteger.valueOf(i+1),value);
				}
			}
		}

		// check hash part
		for ( i -= array.length; i < hash.length; ++i ) {
			Slot slot = hash[i];
			while ( slot != null ) {
				StrongSlot first = slot.first();
				if ( first != null )
					return first.toVarargs();
				slot = slot.rest();
			}
		}
		
		// nothing found, push nil, return nil.
		return NIL;
	}

	/**
	 * Get the next element after a particular key in the 
	 * contiguous array part of a table 
	 * @return key,value or none
	 */
	public io.github.taoguan.luaj.Varargs inext(io.github.taoguan.luaj.LuaValue key) {
		int k = key.checkint() + 1;
		io.github.taoguan.luaj.LuaValue v = rawget(k);
		return v.isnil()? NONE: varargsOf(io.github.taoguan.luaj.LuaInteger.valueOf(k),v);
	}

	/**
	 * Set a hashtable value
	 * @param key key to set
	 * @param value value to set
	 */
	public void hashset(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value) {
		if ( value.isnil() )
			hashRemove(key);
		else {
			int index = 0;
			if ( hash.length > 0 ) {
				index = hashSlot( key );
				for ( Slot slot = hash[ index ]; slot != null; slot = slot.rest() ) {
					StrongSlot foundSlot;
					if ( ( foundSlot = slot.find( key ) ) != null ) {
						hash[index] = hash[index].set( foundSlot, value );
						return;
					}
				}
			}
			if ( checkLoadFactor() ) {
				if ( key.isinttype() && key.toint() > 0 ) {
					// a rehash might make room in the array portion for this key.
					rehash( key.toint() );
					if ( arrayset(key.toint(), value) )
						return;
				} else {
					rehash( -1 );
				}
				index = hashSlot( key );
			}
			Slot entry = ( m_metatable != null )
				? m_metatable.entry( key, value )
				: defaultEntry( key, value );
			hash[ index ] = ( hash[index] != null )	? hash[index].add( entry ) : entry;
			++hashEntries;
		}
	}

	public static int hashpow2( int hashCode, int mask ) {
		return hashCode & mask;
	}

	public static int hashmod( int hashCode, int mask ) {
		return ( hashCode & 0x7FFFFFFF ) % mask;
	}

	/**
	 * Find the hashtable slot index to use.
	 * @param key the key to look for
	 * @param hashMask N-1 where N is the number of hash slots (must be power of 2)
	 * @return the slot index
	 */
	public static int hashSlot(io.github.taoguan.luaj.LuaValue key, int hashMask ) {
		switch ( key.type() ) {
		case TNUMBER:
		case TTABLE:
		case TTHREAD:
		case TLIGHTUSERDATA:
		case TUSERDATA:
			return hashmod( key.hashCode(), hashMask );
		default:
			return hashpow2( key.hashCode(), hashMask );
		}
	}
	
	/** 
	 * Find the hashtable slot to use
	 * @param key key to look for
	 * @return slot to use
	 */
	private int hashSlot(io.github.taoguan.luaj.LuaValue key) {
		return hashSlot( key, hash.length - 1 );
	}

	private void hashRemove( io.github.taoguan.luaj.LuaValue key ) {
		if ( hash.length > 0 ) {
			int index = hashSlot(key);
			for ( Slot slot = hash[index]; slot != null; slot = slot.rest() ) {
				StrongSlot foundSlot;
				if ( ( foundSlot = slot.find( key ) ) != null ) {
					hash[index] = hash[index].remove( foundSlot );
					--hashEntries;
					return;
				}
			}
		}
	}

	private boolean checkLoadFactor() {
		return hashEntries >= hash.length;
	}

	private int countHashKeys() {
		int keys = 0;
		for ( int i = 0; i < hash.length; ++i ) {
			for ( Slot slot = hash[i]; slot != null; slot = slot.rest() ) {
				if ( slot.first() != null )
					keys++;
			}
		}
		return keys;
	}

	private int countIntKeys(int[] nums) {
		int total = 0;
		int i = 1;

		// Count integer keys in array part
		for ( int bit = 0; bit < 31; ++bit ) {
			if ( i > array.length )
				break;
			int j = Math.min(array.length, 1 << bit);
			int c = 0;
			while ( i <= j ) {
				if ( array[ i++ - 1 ] != null )
					c++;
			}
			nums[bit] = c;
			total += c;
		}

		// Count integer keys in hash part
		for ( i = 0; i < hash.length; ++i ) {
			for ( Slot s = hash[i]; s != null; s = s.rest() ) {
				int k;
				if ( ( k = s.arraykey(Integer.MAX_VALUE) ) > 0 ) {
					nums[log2(k)]++;
					total++;
				}
			}
		}

		return total;
	}

	// Compute ceil(log2(x))
	static int log2(int x) {
		int lg = 0;
		x -= 1;
		if ( x < 0 )
			// 2^(-(2^31)) is approximately 0
			return Integer.MIN_VALUE;
		if ( ( x & 0xFFFF0000 ) != 0 ) {
			lg = 16;
			x >>>= 16;
		}
		if ( ( x & 0xFF00 ) != 0 ) {
			lg += 8;
			x >>>= 8;
		}
		if ( ( x & 0xF0 ) != 0 ) {
			lg += 4;
			x >>>= 4;
		}
		switch (x) {
		case 0x0: return 0;
		case 0x1: lg += 1; break;
		case 0x2: lg += 2; break;
		case 0x3: lg += 2; break;
		case 0x4: lg += 3; break;
		case 0x5: lg += 3; break;
		case 0x6: lg += 3; break;
		case 0x7: lg += 3; break;
		case 0x8: lg += 4; break;
		case 0x9: lg += 4; break;
		case 0xA: lg += 4; break;
		case 0xB: lg += 4; break;
		case 0xC: lg += 4; break;
		case 0xD: lg += 4; break;
		case 0xE: lg += 4; break;
		case 0xF: lg += 4; break;
		}
		return lg;
	}

	/*
	 * newKey > 0 is next key to insert
	 * newKey == 0 means number of keys not changing (__mode changed)
	 * newKey < 0 next key will go in hash part
	 */
	private void rehash(int newKey) {
		int[] nums = new int[32];
		int total = countIntKeys(nums);
		if ( newKey > 0 ) {
			total++;
			nums[log2(newKey)]++;
		}

		// Choose N such that N <= sum(nums[0..log(N)]) < 2N
		int keys = nums[0];
		int newArraySize = 0;
		for ( int log = 1; log < 32; ++log ) {
			keys += nums[log];
			if (total * 2 < 1 << log) {
				// Not enough integer keys.
				break;
			} else if (keys >= (1 << (log - 1))) {
				newArraySize = 1 << log;
			}
		}

		final io.github.taoguan.luaj.LuaValue[] oldArray = array;
		final Slot[] oldHash = hash;
		final io.github.taoguan.luaj.LuaValue[] newArray;
		final Slot[] newHash;

		// Copy existing array entries and compute number of moving entries.
		int movingToArray = 0;
		if ( newKey > 0 && newKey <= newArraySize ) {
			movingToArray--;
		}
		if (newArraySize != oldArray.length) {
			newArray = new io.github.taoguan.luaj.LuaValue[newArraySize];
			if (newArraySize > oldArray.length) {
				for (int i = log2(oldArray.length + 1), j = log2(newArraySize) + 1; i < j; ++i) {
					movingToArray += nums[i];
				}
			} else if (oldArray.length > newArraySize) {
				for (int i = log2(newArraySize + 1), j = log2(oldArray.length) + 1; i < j; ++i) {
					movingToArray -= nums[i];
				}
			}
			System.arraycopy(oldArray, 0, newArray, 0, Math.min(oldArray.length, newArraySize));
		} else {
			newArray = array;
		}

		final int newHashSize = hashEntries - movingToArray
			+ ((newKey < 0 || newKey > newArraySize) ? 1 : 0); // Make room for the new entry
		final int oldCapacity = oldHash.length;
		final int newCapacity;
		final int newHashMask;

		if (newHashSize > 0) {
			// round up to next power of 2.
			newCapacity = ( newHashSize < MIN_HASH_CAPACITY )
				? MIN_HASH_CAPACITY
				: 1 << log2(newHashSize);
			newHashMask = newCapacity - 1;
			newHash = new Slot[ newCapacity ];
		} else {
			newCapacity = 0;
			newHashMask = 0;
			newHash = NOBUCKETS;
		}

		// Move hash buckets
		for ( int i = 0; i < oldCapacity; ++i ) {
			for ( Slot slot = oldHash[i]; slot != null; slot = slot.rest() ) {
				int k;
				if ( ( k = slot.arraykey( newArraySize ) ) > 0 ) {
					StrongSlot entry = slot.first();
					if (entry != null)
						newArray[ k - 1 ] = entry.value();
				} else {
					int j = slot.keyindex( newHashMask );
					newHash[j] = slot.relink( newHash[j] );
				}
			}
		}

		// Move array values into hash portion
		for ( int i = newArraySize; i < oldArray.length; ) {
			io.github.taoguan.luaj.LuaValue v;
			if ( ( v = oldArray[ i++ ] ) != null ) {
				int slot = hashmod( io.github.taoguan.luaj.LuaInteger.hashCode( i ), newHashMask );
				Slot newEntry;
				if ( m_metatable != null ) {
					newEntry = m_metatable.entry( valueOf(i), v );
					if ( newEntry == null )
						continue;
				} else {
					newEntry = defaultEntry( valueOf(i), v );
				}
				newHash[ slot ] = ( newHash[slot] != null )
					? newHash[slot].add( newEntry ) : newEntry;
			}
		}

		hash = newHash;
		array = newArray;
		hashEntries -= movingToArray;
	}

	public Slot entry(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value ) {
		return defaultEntry( key, value );
	}

	protected static boolean isLargeKey(io.github.taoguan.luaj.LuaValue key) {
		switch (key.type()) {
		case TSTRING:
			return key.rawlen() > io.github.taoguan.luaj.LuaString.RECENT_STRINGS_MAX_LENGTH;
		case TNUMBER:
		case TBOOLEAN:
			return false;
		default:
			return true;
		}
	}

	protected static Entry defaultEntry(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value) {
		if ( key.isinttype() ) {
			return new IntKeyEntry( key.toint(), value );
		} else if (value.type() == TNUMBER) {
			return new NumberValueEntry( key, value.todouble() );
		} else {
			return new NormalEntry( key, value );
		}
	}

	// ----------------- sort support -----------------------------
	//
	// implemented heap sort from wikipedia
	//
	// Only sorts the contiguous array part. 
	//
	/** Sort the table using a comparator.
	 * @param comparator {@link io.github.taoguan.luaj.LuaValue} to be called to compare elements.
	 */
	public void sort(io.github.taoguan.luaj.LuaValue comparator) {
		if (len().tolong() >= (long)Integer.MAX_VALUE) throw new LuaError("array too big: " + len().tolong());
		int n = array.length;
		while ( n > 0 && array[n-1] == null )
			--n;
		if ( n > 1 ) 
			heapSort(n, comparator);
	}

	private void heapSort(int count, io.github.taoguan.luaj.LuaValue cmpfunc) {
		heapify(count, cmpfunc);
		for ( int end=count-1; end>0; ) {
			swap(end, 0);
			siftDown(0, --end, cmpfunc);
		}
	}

	private void heapify(int count, io.github.taoguan.luaj.LuaValue cmpfunc) {
		for ( int start=count/2-1; start>=0; --start )
			siftDown(start, count - 1, cmpfunc);
	}

	private void siftDown(int start, int end, io.github.taoguan.luaj.LuaValue cmpfunc) {
		for ( int root=start; root*2+1 <= end; ) { 
			int child = root*2+1; 
			if (child < end && compare(child, child + 1, cmpfunc))
				++child; 
			if (compare(root, child, cmpfunc)) {
				swap(root, child);
				root = child;
			} else
				return;
		}
	}

	private boolean compare(int i, int j, io.github.taoguan.luaj.LuaValue cmpfunc) {
		io.github.taoguan.luaj.LuaValue a, b;
		if (m_metatable == null) {
			a = array[i];
			b = array[j];
		} else {
			a = m_metatable.arrayget(array, i);
			b = m_metatable.arrayget(array, j);
		}
		if ( a == null || b == null )
			return false;
		if ( ! cmpfunc.isnil() ) {
			return cmpfunc.call(a,b).toboolean();
		} else {
			return a.lt_b(b);
		}
	}
	
	private void swap(int i, int j) {
		io.github.taoguan.luaj.LuaValue a = array[i];
		array[i] = array[j];
		array[j] = a;
	}
	
	/** This may be deprecated in a future release.  
	 * It is recommended to count via iteration over next() instead
	 * @return count of keys in the table 
	 * */
	public int keyCount() {
		io.github.taoguan.luaj.LuaValue k = io.github.taoguan.luaj.LuaValue.NIL;
		for ( int i=0; true; i++ ) {
			io.github.taoguan.luaj.Varargs n = next(k);
			if ( (k = n.arg1()).isnil() )
				return i;
		}
	}
	
	/** This may be deprecated in a future release.  
	 * It is recommended to use next() instead 
	 * @return array of keys in the table 
	 * */
	public io.github.taoguan.luaj.LuaValue[] keys() {
		Vector l = new Vector();
		io.github.taoguan.luaj.LuaValue k = io.github.taoguan.luaj.LuaValue.NIL;
		while ( true ) {
			io.github.taoguan.luaj.Varargs n = next(k);
			if ( (k = n.arg1()).isnil() )
				break;
			l.addElement( k );
		}
		io.github.taoguan.luaj.LuaValue[] a = new io.github.taoguan.luaj.LuaValue[l.size()];
		l.copyInto(a);
		return a;
	}
	
	// equality w/ metatable processing
	public io.github.taoguan.luaj.LuaValue eq(io.github.taoguan.luaj.LuaValue val ) {  return eq_b(val)? TRUE: FALSE; }
	public boolean eq_b( io.github.taoguan.luaj.LuaValue val )  {
		if ( this == val ) return true;
		if ( m_metatable == null || !val.istable() ) return false;
		io.github.taoguan.luaj.LuaValue valmt = val.getmetatable();
		return valmt!=null && io.github.taoguan.luaj.LuaValue.eqmtcall(this, m_metatable.toLuaValue(), val, valmt);
	}

	/** Unpack all the elements of this table */
	public io.github.taoguan.luaj.Varargs unpack() {
		return unpack(1, this.rawlen());
	}

	/** Unpack all the elements of this table from element i */
	public io.github.taoguan.luaj.Varargs unpack(int i) {
		return unpack(i, this.rawlen());
	}

	/** Unpack the elements from i to j inclusive */
	public io.github.taoguan.luaj.Varargs unpack(int i, int j) {
		if (j < i) return NONE;
		int count = j - i;
		if (count < 0) throw new LuaError("too many results to unpack: greater " + Integer.MAX_VALUE); // integer overflow
		int max = 0x00ffffff;
		if (count >= max) throw new LuaError("too many results to unpack: " + count + " (max is " + max + ')');
		int n = j + 1 - i;
		switch (n) {
		case 0: return NONE;
		case 1: return get(i);
		case 2: return varargsOf(get(i), get(i+1));
		default:
			if (n < 0)
				return NONE;
			try {
				io.github.taoguan.luaj.LuaValue[] v = new io.github.taoguan.luaj.LuaValue[n];
				while (--n >= 0)
					v[n] = get(i+n);
				return varargsOf(v);
			} catch (OutOfMemoryError e) {
				throw new LuaError("too many results to unpack [out of memory]: " + n);
			}
		}
	}

	/**
	 * Represents a slot in the hash table.
	 */
	interface Slot {

		/** Return hash{pow2,mod}( first().key().hashCode(), sizeMask ) */
		int keyindex( int hashMask );

		/** Return first Entry, if still present, or null. */
		StrongSlot first();

		/** Compare given key with first()'s key; return first() if equal. */
		StrongSlot find( io.github.taoguan.luaj.LuaValue key );

		/**
		 * Compare given key with first()'s key; return true if equal. May
		 * return true for keys no longer present in the table.
		 */
		boolean keyeq( io.github.taoguan.luaj.LuaValue key );

		/** Return rest of elements */
		Slot rest();

		/**
		 * Return first entry's key, iff it is an integer between 1 and max,
		 * inclusive, or zero otherwise.
		 */
		int arraykey( int max );

		/**
		 * Set the value of this Slot's first Entry, if possible, or return a
		 * new Slot whose first entry has the given value.
		 */
		Slot set( StrongSlot target, io.github.taoguan.luaj.LuaValue value );

		/**
		 * Link the given new entry to this slot.
		 */
		Slot add( Slot newEntry );

		/**
		 * Return a Slot with the given value set to nil; must not return null
		 * for next() to behave correctly.
		 */
		Slot remove( StrongSlot target );

		/**
		 * Return a Slot with the same first key and value (if still present)
		 * and rest() equal to rest.
		 */
		Slot relink( Slot rest );
	}

	/**
	 * Subclass of Slot guaranteed to have a strongly-referenced key and value,
	 * to support weak tables.
	 */
	interface StrongSlot extends Slot {
		/** Return first entry's key */
		io.github.taoguan.luaj.LuaValue key();

		/** Return first entry's value */
		io.github.taoguan.luaj.LuaValue value();

		/** Return varargsOf(key(), value()) or equivalent */
		io.github.taoguan.luaj.Varargs toVarargs();
	}

	private static class LinkSlot implements StrongSlot {
		private Entry entry;
		private Slot next;

		LinkSlot( Entry entry, Slot next ) {
			this.entry = entry;
			this.next = next;
		}

		public io.github.taoguan.luaj.LuaValue key() {
			return entry.key();
		}

		public int keyindex( int hashMask ) {
			return entry.keyindex( hashMask );
		}

		public io.github.taoguan.luaj.LuaValue value() {
			return entry.value();
		}

		public io.github.taoguan.luaj.Varargs toVarargs() {
			return entry.toVarargs();
		}

		public StrongSlot first() {
			return entry;
		}

		public StrongSlot find(io.github.taoguan.luaj.LuaValue key) {
			return entry.keyeq(key) ? this : null;
		}

		public boolean keyeq(io.github.taoguan.luaj.LuaValue key) {
			return entry.keyeq(key);
		}

		public Slot rest() {
			return next;
		}

		public int arraykey( int max ) {
			return entry.arraykey( max );
		}

		public Slot set(StrongSlot target, io.github.taoguan.luaj.LuaValue value) {
			if ( target == this ) {
				entry = entry.set( value );
				return this;
			} else {
				return setnext(next.set( target, value ));
			}
		}

		public Slot add( Slot entry ) {
			return setnext(next.add( entry ));
		}

		public Slot remove( StrongSlot target ) {
			if ( this == target ) {
				return new DeadSlot( key(), next );
			} else {
				this.next = next.remove( target );
			}
			return this;
		}

		public Slot relink(Slot rest) {
			// This method is (only) called during rehash, so it must not change this.next.
			return ( rest != null ) ? new LinkSlot(entry, rest) : (Slot)entry;
		}

		// this method ensures that this.next is never set to null.
		private Slot setnext(Slot next) {
			if ( next != null ) {
				this.next = next;
				return this;
			} else {
				return entry;
			}
		}

		public String toString() {
			return entry + "; " + next;
		}
	}

	/**
	 * Base class for regular entries.
	 * 
	 * <p>
	 * If the key may be an integer, the {@link #arraykey(int)} method must be
	 * overridden to handle that case.
	 */
	static abstract class Entry extends io.github.taoguan.luaj.Varargs implements StrongSlot {
		public abstract io.github.taoguan.luaj.LuaValue key();
		public abstract io.github.taoguan.luaj.LuaValue value();
		abstract Entry set(io.github.taoguan.luaj.LuaValue value);
		public abstract boolean keyeq( io.github.taoguan.luaj.LuaValue key );
        public abstract int keyindex( int hashMask );

		public int arraykey( int max ) {
			return 0;
		}

		public io.github.taoguan.luaj.LuaValue arg(int i) {
			switch (i) {
			case 1: return key();
			case 2: return value();
			}
			return NIL;
		}

		public int narg() {
			return 2;
		}

		/**
		 * Subclasses should redefine as "return this;" whenever possible.
		 */
		public io.github.taoguan.luaj.Varargs toVarargs() {
			return varargsOf(key(), value());
		}

		public io.github.taoguan.luaj.LuaValue arg1() {
			return key();
		}

		public io.github.taoguan.luaj.Varargs subargs(int start) {
			switch (start) {
			case 1: return this;
			case 2: return value();
			}
			return NONE;
		}

		public StrongSlot first() {
			return this;
		}

		public Slot rest() {
			return null;
		}

		public StrongSlot find(io.github.taoguan.luaj.LuaValue key) {
			return keyeq(key) ? this : null;
		}

		public Slot set(StrongSlot target, io.github.taoguan.luaj.LuaValue value) {
			return set( value );
		}

		public Slot add( Slot entry ) {
			return new LinkSlot( this, entry );
		}

		public Slot remove(StrongSlot target) {
			return new DeadSlot( key(), null );
		}

		public Slot relink( Slot rest ) {
			return ( rest != null ) ? new LinkSlot( this, rest ) : (Slot)this;
		}
	}

	static class NormalEntry extends Entry {
		private final io.github.taoguan.luaj.LuaValue key;
		private io.github.taoguan.luaj.LuaValue value;

		NormalEntry(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value ) {
			this.key = key;
			this.value = value;
		}

		public io.github.taoguan.luaj.LuaValue key() {
			return key;
		}

		public io.github.taoguan.luaj.LuaValue value() {
			return value;
		}

		public Entry set(io.github.taoguan.luaj.LuaValue value) {
			this.value = value;
			return this;
		}

		public io.github.taoguan.luaj.Varargs toVarargs() {
			return this;
		}

		public int keyindex( int hashMask ) {
			return hashSlot( key, hashMask );
		}

		public boolean keyeq(io.github.taoguan.luaj.LuaValue key) {
			return key.raweq(this.key);
		}
	}

	private static class IntKeyEntry extends Entry {
		private final int key;
		private io.github.taoguan.luaj.LuaValue value;

		IntKeyEntry(int key, io.github.taoguan.luaj.LuaValue value) {
			this.key = key;
			this.value = value;
		}

		public io.github.taoguan.luaj.LuaValue key() {
			return valueOf( key );
		}

		public int arraykey(int max) {
			return ( key >= 1 && key <= max ) ? key : 0;
		}

		public io.github.taoguan.luaj.LuaValue value() {
			return value;
		}

		public Entry set(io.github.taoguan.luaj.LuaValue value) {
			this.value = value;
			return this;
		}

		public int keyindex( int mask ) {
			return hashmod( io.github.taoguan.luaj.LuaInteger.hashCode( key ), mask );
		}

		public boolean keyeq(io.github.taoguan.luaj.LuaValue key) {
			return key.raweq( this.key );
		}
	}

	/**
	 * Entry class used with numeric values, but only when the key is not an integer.
	 */
	private static class NumberValueEntry extends Entry {
		private double value;
		private final io.github.taoguan.luaj.LuaValue key;

		NumberValueEntry(io.github.taoguan.luaj.LuaValue key, double value) {
			this.key = key;
			this.value = value;
		}

		public io.github.taoguan.luaj.LuaValue key() {
			return key;
		}

		public io.github.taoguan.luaj.LuaValue value() {
			return valueOf(value);
		}

		public Entry set(io.github.taoguan.luaj.LuaValue value) {
			if (value.type() == TNUMBER) {
				io.github.taoguan.luaj.LuaValue n = value.tonumber();
				if (!n.isnil()) {
					this.value = n.todouble();
					return this;
				}
			}
			return new NormalEntry( this.key, value );
		}

		public int keyindex( int mask ) {
			return hashSlot( key, mask );
		}

		public boolean keyeq(io.github.taoguan.luaj.LuaValue key) {
			return key.raweq(this.key);
		}
	}

	/**
	 * A Slot whose value has been set to nil. The key is kept in a weak reference so that
	 * it can be found by next().
	 */
	private static class DeadSlot implements Slot {

		private final Object key;
		private Slot next;

		private DeadSlot(io.github.taoguan.luaj.LuaValue key, Slot next ) {
			this.key = isLargeKey(key) ? new WeakReference( key ) : (Object)key;
			this.next = next;
		}

		private io.github.taoguan.luaj.LuaValue key() {
			return (io.github.taoguan.luaj.LuaValue) (key instanceof WeakReference ? ((WeakReference) key).get() : key);
		}

		public int keyindex(int hashMask) {
			// Not needed: this entry will be dropped during rehash.
			return 0;
		}

		public StrongSlot first() {
			return null;
		}

		public StrongSlot find(io.github.taoguan.luaj.LuaValue key) {
			return null;
		}

		public boolean keyeq(io.github.taoguan.luaj.LuaValue key) {
			io.github.taoguan.luaj.LuaValue k = key();
			return k != null && key.raweq(k);
		}

		public Slot rest() {
			return next;
		}

		public int arraykey(int max) {
			return -1;
		}

		public Slot set(StrongSlot target, io.github.taoguan.luaj.LuaValue value) {
			Slot next = ( this.next != null ) ? this.next.set( target, value ) : null;
			if ( key() != null ) {
				// if key hasn't been garbage collected, it is still potentially a valid argument
				// to next(), so we can't drop this entry yet.
				this.next = next;
				return this;
			} else {
				return next;
			}
		}

		public Slot add(Slot newEntry) {
			return ( next != null ) ? next.add(newEntry) : newEntry;
		}

		public Slot remove(StrongSlot target) {
			if ( key() != null ) {
				next = next.remove(target);
				return this;
			} else {
				return next;
			}
		}

		public Slot relink(Slot rest) {
			return rest;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("<dead");
			io.github.taoguan.luaj.LuaValue k = key();
			if (k != null) {
				buf.append(": ");
				buf.append(k.toString());
			}
			buf.append('>');
			if (next != null) {
				buf.append("; ");
				buf.append(next.toString());
			}
			return buf.toString();
		}
	};

	public io.github.taoguan.luaj.LuaValue toLuaValue() {
		return this;
	}

	public io.github.taoguan.luaj.LuaValue wrap(io.github.taoguan.luaj.LuaValue value) {
		return value;
	}

	public io.github.taoguan.luaj.LuaValue arrayget(io.github.taoguan.luaj.LuaValue[] array, int index) {
		return array[index];
	}
}
