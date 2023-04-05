package io.github.taoguan.luaj;


public class LuaUserdata extends io.github.taoguan.luaj.LuaValue {
	
	public Object m_instance;
	public io.github.taoguan.luaj.LuaValue m_metatable;
	
	public LuaUserdata(Object obj) {
		m_instance = obj;
	}
	
	public LuaUserdata(Object obj, io.github.taoguan.luaj.LuaValue metatable) {
		m_instance = obj;
		m_metatable = metatable;
	}
	
	public String tojstring() {
		return String.valueOf(m_instance);
	}
	
	public int type() {
		return io.github.taoguan.luaj.LuaValue.TUSERDATA;
	}
	
	public String typename() {
		return "userdata";
	}

	public int hashCode() {
		return m_instance.hashCode();
	}
	
	public Object userdata() {
		return m_instance;
	}
	
	public boolean isuserdata()                        { return true; }
	public boolean isuserdata(Class c)                 { return c.isAssignableFrom(m_instance.getClass()); }
	public Object  touserdata()                        { return m_instance; }
	public Object  touserdata(Class c)                 { return c.isAssignableFrom(m_instance.getClass())? m_instance: null; }
	public Object  optuserdata(Object defval)          { return m_instance; }
	public Object optuserdata(Class c, Object defval) {
		if (!c.isAssignableFrom(m_instance.getClass()))
			typerror(c.getName());
		return m_instance;
	}
	
	public io.github.taoguan.luaj.LuaValue getmetatable() {
		return m_metatable;
	}

	public io.github.taoguan.luaj.LuaValue setmetatable(io.github.taoguan.luaj.LuaValue metatable) {
		this.m_metatable = metatable;
		return this;
	}

	public Object checkuserdata() {
		return m_instance;
	}
	
	public Object checkuserdata(Class c) { 
		if ( c.isAssignableFrom(m_instance.getClass()) )
			return m_instance;		
		return typerror(c.getName());
	}
	
	public io.github.taoguan.luaj.LuaValue get(io.github.taoguan.luaj.LuaValue key ) {
		return m_metatable!=null? gettable(this,key): NIL;
	}
	
	public void set(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value ) {
		if ( m_metatable==null || ! settable(this,key,value) )
			error( "cannot set "+key+" for userdata" );
	}

	public boolean equals( Object val ) {
		if ( this == val )
			return true;
		if ( ! (val instanceof LuaUserdata) )
			return false;
		LuaUserdata u = (LuaUserdata) val;
		return m_instance.equals(u.m_instance);
	}

	// equality w/ metatable processing
	public io.github.taoguan.luaj.LuaValue eq(io.github.taoguan.luaj.LuaValue val )     { return eq_b(val)? TRUE: FALSE; }
	public boolean eq_b( io.github.taoguan.luaj.LuaValue val ) {
		if ( val.raweq(this) ) return true;
		if ( m_metatable == null || !val.isuserdata() ) return false;
		io.github.taoguan.luaj.LuaValue valmt = val.getmetatable();
		return valmt!=null && io.github.taoguan.luaj.LuaValue.eqmtcall(this, m_metatable, val, valmt);
	}
	
	// equality w/o metatable processing
	public boolean raweq( io.github.taoguan.luaj.LuaValue val )      { return val.raweq(this); }
	public boolean raweq( LuaUserdata val )   {
		return this == val || (m_metatable == val.m_metatable && m_instance.equals(val.m_instance)); 
	}
	
	// __eq metatag processing
	public boolean eqmt( io.github.taoguan.luaj.LuaValue val ) {
		return m_metatable!=null && val.isuserdata()? io.github.taoguan.luaj.LuaValue.eqmtcall(this, m_metatable, val, val.getmetatable()): false;
	}
}
