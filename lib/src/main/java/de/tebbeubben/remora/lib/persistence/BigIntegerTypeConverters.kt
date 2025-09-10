package de.tebbeubben.remora.lib.persistence

import androidx.room.TypeConverter
import java.math.BigInteger

class BigIntegerTypeConverters {

    @TypeConverter
    fun bigIntegerToString(bigInteger: BigInteger?): ByteArray? = bigInteger?.toByteArray()

    @TypeConverter
    fun stringToBigInteger(data: ByteArray?): BigInteger? = data?.let { BigInteger(it) }

}