package com.vremea.romaniei.domain
import com.vremea.romaniei.domain.model.WeatherCode
import org.junit.Test
import org.junit.Assert.*
class WeatherCodeTest {
    @Test fun `all codes return non-null descriptions`() {
        for (code in 0..99) {
            assertNotNull("EN description null for code $code", WeatherCode.getDescription(code, false))
            assertNotNull("RO description null for code $code", WeatherCode.getDescription(code, true))
        }
    }
    @Test fun `clear sky returns sunny icon`() {
        assertEquals("sunny", WeatherCode.getIcon(0))
    }
}
