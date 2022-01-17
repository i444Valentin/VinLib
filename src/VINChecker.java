import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VINChecker {
    private static final int VIN_LENGTH = 17;
    private HashMap<String,String> countries = new HashMap<>();
    private static Pattern pattern;
    private static final String VIN_REGEXP="^(?<wmi>[a-z1-9&&[^oiq]]{1}[a-z0-9&&[^oiq]]{2})" +
            "(?<vds>[a-z0-9&&[^oiq]]{5})" +
            "(?<sign>[0-9x]{1})" +
            "(?<modelYear>[a-y0-9&&[^oiqu]]{1})" +
            "(?<vis>[a-z0-9&&[^oiq]]{3}[0-9]{4})$";

    private String fullVIN;
    private String WMI;
    private String VDS;
    private String sign;
    private String modelYear;
    private String VIS;

    public VINChecker() {
        pattern = Pattern.compile(VIN_REGEXP, Pattern.CASE_INSENSITIVE);
        fillCountriesDictionary();
        fillYearDictionary();
    }

    private void fillYearDictionary() {

    }

    public void fillCountriesDictionary() {
        String strFromFile = getFromFile();
        System.out.print(strFromFile);
        String[] codes = strFromFile.split(";");
        for (String code : codes)
        {
            char[] sep = { ' ' };
            String[] codeInfo = code.split(String.valueOf(sep), 2);
            // Преобразует AA-AH в правило A[A-H]
            if ((codeInfo[0].charAt(1) >= 'A' && codeInfo[0].charAt(1) <= 'Z') && (codeInfo[0].charAt(4) >= '0' && codeInfo[0].charAt(4) <= '9'))
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
     * Returns countries dictionary from file.
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

    /**
     * Checks vin on validation
     * @param vin - vin requires check
     * @return - true - vin is valid, false - vin is not valid
     */
    //TODO this method requires optimization
    public boolean checkVIN(String vin){
        if (vin.length() > VIN_LENGTH){
            System.err.print("Length Vin is invalid.");
            return false;
        }
        final Matcher matcher = pattern.matcher(vin);
        if (matcher.find()){
            System.out.println("WMI: " + matcher.group(1));
            this.WMI = matcher.group(1);
            System.out.println("VDS: " + matcher.group(2));
            this.VDS = matcher.group(2);
            System.out.println("SING: " + matcher.group(3));
            this.sign = matcher.group(3);
            System.out.println("Model Year: " + matcher.group(4));
            this.modelYear = matcher.group(4);
            System.out.println("VIS: " + matcher.group(5));
            this.VIS = matcher.group(5);
            System.out.println("________________________________");
        }

        return matcher.matches();
    }

    /**
     * Returns country, makes current car
     * @return - country in string format
     */
    String getVINCountry(){
        String countryCode = WMI.substring(0,2);

        for (Map.Entry<String,String> entry : countries.entrySet()){
            if (countryCode.matches(entry.getKey())){
                return entry.getValue();
            }
        }
        return null;
    }

    String getVINYear(String vin){
        return null;
    }
    private String replaceChars(String string, Map<Character,Integer> replacesMap){
        StringBuilder replacedStr = new StringBuilder();
        for (int i = 0; i < string.length();i++){
            Character character = string.charAt(i);
            Integer integer = replacesMap.get(character);
            if (integer == null) replacedStr.append(character);
            else replacedStr.append(integer);
        }
        return String.valueOf(replacedStr);
    }

    /**
     *This method calculates vin check via a special algorithm.
     * @param vin - vin from calc vin check
     * @return - check sign (if check result equals 10 he replaced on char X)
     */
    public Character calculateSignFromVin(String vin){
        //Map using for replaces word chars (A -> 1)
        Map<Character, Integer> replacesMap = Stream.of(new Object[][]{
                {'A',1}, {'B',2},{'C',3},{'D',4},{'E',5},{'F',6},{'G',7},
                {'H',8},{'J',1},{'K',2},{'L',3},{'M',4},{'N',5},{'P',7},
                {'R',9},{'S',2},{'T',3},{'U',4},{'V',5},{'W',6},{'X',7},{'Y',8},{'Z',9}
        }).collect(Collectors.toMap(data -> (Character) data[0],data -> (Integer) data[1]));

        String replacedStr = replaceChars(vin,replacesMap);

        int check = calculateCheck(replacedStr);

        char checkChar = Character.forDigit(check,10);
        if (checkChar == '\u0000') return 'X';
        return checkChar;
    }

    private int calculateCheck(String replacedStr) {
        Integer sumVinWeight = 0;
        Integer[] vinWeigths = {8,7,6,5,4,3,2,10,0,9,8,7,6,5,4,3,2};
        for (int i = 0; i < replacedStr.length();i++){
            int j = Character.digit(replacedStr.charAt(i),10);
            sumVinWeight += j * vinWeigths[i];
        }
        int multiple11 = sumVinWeight / 11;
        Integer sumVinWeight11 = multiple11 * 11;
        return sumVinWeight - sumVinWeight11; //check value
    }
}
