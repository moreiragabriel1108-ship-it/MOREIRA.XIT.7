package com.luaxit.sevenx.data
import android.content.Context
class SettingsRepository(context:Context){private val p=context.getSharedPreferences("lua_xit",Context.MODE_PRIVATE);fun precision()=p.getInt("precision",80).coerceIn(40,100);fun savePrecision(v:Int){p.edit().putInt("precision",v.coerceIn(40,100)).apply()}}