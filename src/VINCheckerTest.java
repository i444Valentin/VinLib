import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VINCheckerTest {

    @Test
    void checkMark(){
        String VIN1 = "1HGC42254WA015540"; //wrong check sum
        String VIN2 = "WD2YD241825356884";
        VINChecker vinChecker = new VINChecker();
        boolean stCheck1 = vinChecker.checkVIN(VIN1);
        boolean stCheck2 = vinChecker.checkVIN(VIN2);
        assertTrue(stCheck1);
        assertTrue(stCheck2);

    }

    @Test
    void checkSignVIN(){
        VINChecker vinChecker = new VINChecker();
        String VIN = "WD2YD241825356884";
        Character expected = vinChecker.calculateSignFromVin(VIN);
        Character actual = VIN.charAt(8);
        assertEquals(expected,actual);
    }

    @Test
    void getCountriesFromFile(){
        VINChecker vinChecker = new VINChecker();
        vinChecker.fillCountriesDictionary();
    }

    @Test
    void getVINCountry(){
        VINChecker vinChecker = new VINChecker();
        vinChecker.checkVIN("WD2YD241825356884");
        String expected = vinChecker.getVINCountry();
        System.out.print(expected);
        String actual = "Германия";
        assertEquals(expected,actual);
    }

    @Test
    void getVINModelYear(){
        VINChecker vinChecker = new VINChecker();
        vinChecker.checkVIN("WD2YD241825356884");
        Integer expected = vinChecker.getVINYear();
        System.out.print(expected);
        Integer actual = 2002;
        assertEquals(expected,actual);
    }

}