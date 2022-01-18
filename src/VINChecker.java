import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allow work with VIN, run check, get year and country vendor.
 * @author Walentin
 */
@SuppressWarnings("all")
public class VINChecker {
    private static final int VIN_LENGTH = 17;
    private HashMap<String,String> countries = new HashMap<>();
    private HashMap<Character,Integer> years = new HashMap<>();
    private static Pattern pattern;
    private static final String VIN_REGEXP="^(?<wmi>[a-z1-9&&[^oiq]][a-z0-9&&[^oiq]]{2})" +
            "(?<vds>[a-z0-9&&[^oiq]]{5})" +
            "(?<sign>[0-9x])" +
            "(?<modelYear>[a-y0-9&&[^oiqu]])" +
            "(?<vis>[a-z0-9&&[^oiq]]{3}[0-9]{4})$";

    private String fullVIN;
    private String WMI;
    private String VDS;
    private String sign;
    private String modelYear;
    private String VIS;

    public VINChecker() {
        initializeVIN();
    }

    private void initializeVIN(){
        pattern = Pattern.compile(VIN_REGEXP, Pattern.CASE_INSENSITIVE);
        fillCountriesDictionary();
        fillYearDictionary();
    }


    /**
     * Checks vin on validation
     * @param vin - vin requires check
     * @return - true - vin is valid, false - vin is not valid
     */
    boolean checkVIN(String vin){
        if (vin.length() > VIN_LENGTH){
            System.err.print("Length Vin is invalid.");
            return false;
        }
        final Matcher matcher = pattern.matcher(vin);
        if (matcher.find()){
            this.fullVIN = matcher.group(0); // full vin
            this.WMI = matcher.group(1); //wmi
            this.VDS = matcher.group(2); //vds
            this.sign = matcher.group(3); //sing
            this.modelYear = matcher.group(4); //model year
            this.VIS = matcher.group(5); //vis
        }
        if(!matcher.matches()) return false;
        Character checkChar = calculateSignFromVin(vin);
        if (checkChar == vin.charAt(8)) return true;
        return false;
    }

    /**
     * Returns country, makes current car
     * @return - country in string format, null if country not exists
     */
    public String getVINCountry(){
        String countryCode = WMI.substring(0,2);

        for (Map.Entry<String,String> entry : countries.entrySet()){
            if (countryCode.matches(entry.getKey())){
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Returns year equals a model year.
     *
     * @return - year
     * null if model year doesn't exist
     */
    public Integer getVINYear(){
        Character modelYear = this.modelYear.charAt(0);
        for (Map.Entry<Character,Integer> entry : years.entrySet()){
            if (modelYear == entry.getKey()){
                return entry.getValue();
            }
        }
        return null;
    }



    /**
     *Calculates vin check via a special algorithm.
     * @param vin - vin from calc vin check
     * @return - check sign (if check result equals 10 he replaced on char X)
     */
    private Character calculateSignFromVin(String vin){
        //Map using for replaces word chars (example: A -> 1 or B -> 2)
        Map<Character, Integer> replacesMap = Stream.of(new Object[][]{
                {'A',1}, {'B',2},{'C',3},{'D',4},{'E',5},{'F',6},{'G',7},
                {'H',8},{'J',1},{'K',2},{'L',3},{'M',4},{'N',5},{'P',7},
                {'R',9},{'S',2},{'T',3},{'U',4},{'V',5},{'W',6},{'X',7},{'Y',8},{'Z',9}
        }).collect(Collectors.toMap(data -> (Character) data[0],data -> (Integer) data[1]));

        String replacedStr = replaceChars(vin,replacesMap);

        int check = calculateCheck(replacedStr);
        char checkChar;
        if (check == 10) checkChar = 'X';
        else checkChar = Character.forDigit(check,10);

        return checkChar;
    }

    /**
     * Replaces a vin string on digit equivalent via a replaces map.
     * @param vin - vin
     * @param replacesMap - map for replaces
     * @return - replaced vin
     */
    private String replaceChars(String vin, Map<Character,Integer> replacesMap){
        StringBuilder replacedStr = new StringBuilder();
        for (int i = 0; i < vin.length();i++){
            Character character = vin.charAt(i);
            Integer integer = replacesMap.get(character);
            if (integer == null) replacedStr.append(character);
            else replacedStr.append(integer);
        }
        return String.valueOf(replacedStr);
    }

    /**
     * Returns calculated check vin.
     *
     * @param replacedVIN - vin replaced on digit equivalents.
     * @return - check vin
     */
    private int calculateCheck(String replacedVIN) {
        Integer sumVinWeight = 0;
        Integer[] vinWeigths = {8,7,6,5,4,3,2,10,0,9,8,7,6,5,4,3,2};
        for (int i = 0; i < replacedVIN.length();i++){
            int j = Character.digit(replacedVIN.charAt(i),10);
            sumVinWeight += j * vinWeigths[i];
        }
        int multiple11 = sumVinWeight / 11;
        Integer sumVinWeight11 = multiple11 * 11;
        return sumVinWeight - sumVinWeight11; //check value
    }

    /**
     * Fills year dictionary.
     */
    private void fillYearDictionary() {
        int year = 1980;
        for (int i = 65; i <= 89; ++i){// A..Z
            if (i == 81 || i == 79 || i == 73 || i == 85) continue;
            years.put((char)i, year);
            ++year;
        }

        for (int i = 49; i <= 57; ++i){ //0..9
            years.put((char)i, year);
            ++year;
        }
    }

    /**
     * Fills countries dictionary
     */
    public void fillCountriesDictionary() {
        String strFromFile = getFromFile();
        String[] codes = strFromFile.split(";");
        for (String code : codes)
        {
            char[] sep = { ' ' };
            String[] codeInfo = code.split(String.valueOf(sep), 2);
            // Преобразует AA-AH в правило A[A-H]
            if ((codeInfo[0].charAt(1) >= 'A' && codeInfo[0].charAt(1) <= 'Z')
                    && (codeInfo[0].charAt(4) >= '0' && codeInfo[0].charAt(4) <= '9'))
                countries.put(
                        codeInfo[0].charAt(0) + "[" + codeInfo[0].charAt(1) + "-Z0-" + codeInfo[0].charAt(4) + "]",
                        codeInfo[1]);
            else
                countries.put(
                        codeInfo[0].charAt(0) + "[" + codeInfo[0].charAt(1) + "-" + codeInfo[0].charAt(4) + "]",
                        codeInfo[1]);
        }
        System.out.print("");
    }

    /**
     * Returns countries dictionary from a file.
     * @return - string with a countries codes
     */
    private String getFromFile() {
        StringBuilder str = new StringBuilder();
        try(FileReader fileReader = new FileReader(new File("CountryCodes"))){
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNextLine()){
                str.append(scanner.nextLine());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return String.valueOf(str);
    }
}
