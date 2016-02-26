import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.signum;
import static java.lang.Math.random;

import org.junit.*;
import org.junit.rules.Timeout;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;
import static org.junit.runners.Parameterized.*;

public class LStringTest {

   private static class TestPhase {
      String name;
      int number;
      Class[] testClasses;
      
      TestPhase(String name, int number, Class... testClasses) {
         this.name = name;
         this.number = number;
         this.testClasses = testClasses;
      }
   }

   private static final TestPhase[] testPhases =
         {new TestPhase("constructor, length, toString", 10,
               EmptyStringTest.class, // Test construction of empty strings
               LStringOfStringTest.class), // Test construction of LStrings from Strings
          new TestPhase("compareTo and equals", 18,
               LStringCompareToTest.class), // Test compareTo and equals for LStrings
          new TestPhase("charAt and setCharAt", 18,
               LStringCharAtTest.class), // Test charAt and setCharAt
          new TestPhase("substring", 63,
               LStringSubStringTestSpecial.class, // Test special cases of substring
               LStringSubStringTest.class), // Test substrings of longer strings
          new TestPhase("replace", 31,
               LStringReplaceTestSpecial.class, // Test special cases of replace
               LStringReplaceTest.class), // Test replace with longer strings
          new TestPhase("special", 3,
               LStringSpecialTest.class) // Odd and special tests
         };
   
   public static void main(String[] args) {
      for (TestPhase phase : testPhases) {
         String tests = (phase.number == 1) ? "test" : "tests";
         System.out.println("Running " + phase.name + " tests (" + phase.number + " " + tests + ")");
         Boolean success = new TestRunner().run(phase.testClasses);
         System.out.println();
         if (!success) {
            System.out.println("Test failures: abandoning other phases.");
            System.exit(1);
         }
      }
      System.out.println("Congratulations! All tests passed.");
   }
   
   static class TestRunner {
      public boolean run(Class<?>... classes) {
          JUnitCore core = new JUnitCore();
          core.addListener(new TestListener(System.out));
          Result result = core.run(classes);
          printResult(System.out, result);
          return result.wasSuccessful();
      }
      
      public void printResult(PrintStream stream, Result result) {
         // Header
         stream.printf("Time: %.3f%n", result.getRunTime()/1000.0);

         // Print Failures
         List<Failure> failures = result.getFailures();
         if (failures.size() > 0) {
            stream.println();
            String format = (failures.size() == 1) ?
                  "There was %d failure:%n" :
                  "There were %d failures:%n";
            stream.printf(format, failures.size());
            int failNo = 0;
            for (Failure fail : failures) {
               stream.printf("%d) %s%n", ++failNo, fail.getTestHeader());
               Throwable ex = fail.getException();
               stream.println(ex);
               int ignored = 0;
               for (StackTraceElement elt : ex.getStackTrace()) {
                  String className = elt.getClassName();
                  if (className.startsWith("LString") || className.startsWith("org.junit.Assert")) {
                     if (ignored != 0) {
                        stream.printf("        ... %d more%n", ignored);
                        ignored = 0;
                     }
                     stream.println("        at " + elt);
                  } else
                     ignored++;
               }
               if (ignored != 0)
                  stream.printf("        ... %d more%n", ignored);
            }
            stream.println();
         }
         
         // Footer
         int runCount = result.getRunCount();
         String tests = (runCount == 1) ? "test" : "tests";
         int ignoreCount = result.getIgnoreCount();
         String ignoreTests = (ignoreCount == 1) ? "test" : "tests";
         if (runCount == 0) {
            if (ignoreCount == 0)
               stream.printf("No tests were run.");
            else
               stream.printf("No tests were run (%d %s ignored.)",
                  ignoreCount, ignoreTests);
         } else {
            if (result.wasSuccessful())
               stream.printf("OK! (%d %s passed", runCount, tests);
            else
               stream.printf("Test Failed! (%d of %d %s failed",
                     result.getFailureCount(), runCount, tests);
            if (result.getIgnoreCount() != 0)
               stream.printf(", %d %s ignored", ignoreCount, ignoreTests);
            stream.println(".)");
         }
      }
   }
   
   static class TestListener extends RunListener {
      private final PrintStream stream;
      private boolean testStarted = false;
      
      public TestListener(PrintStream stream) {
         this.stream = stream;
      }
        
      @Override
      public void testRunStarted(Description description) {
         stream.append("Starting tests: ");
         testStarted = false;
      }
      
      @Override
      public void testRunFinished(Result result) {
         stream.println();
      }
      
      @Override
      public void testStarted(Description description) {
         testStarted = true;
      }
      
      @Override
      public void testFailure(Failure failure) {
         stream.append('E');
         testStarted = false;
      }
      
      @Override
      public void testFinished(Description description) {
         if (testStarted)
            stream.append('.');
         testStarted = false;
      }
      
      @Override
      public void testIgnored(Description description) {
         stream.append('I');
      }
   }
   
   
   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class EmptyStringTest {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);

      @Test public void t01aEmptyConstructorIsEmptyString() {
         assertEquals("LString().toString() is not the empty string.",
               "", new LString().toString());
      }
      
      @Test public void t01bEmptyConstructorHasZeroLength() {
         assertEquals("LString().length() is not zero.",
               0, new LString().length());
      }
      
      @Test public void t02aEmptyConstructorIsEmptyString() {
         assertEquals("LString(\"\").toString() is not the empty string.",
               "", new LString("").toString());
      }
      
      @Test public void t02bEmptyConstructorHasZeroLength() {
         assertEquals("LString(\"\").length() is not zero.",
               0, new LString("").length());
      }
   }
   
   @RunWith(Parameterized.class)
   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringOfStringTest {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);

      @Parameters
      public static Collection<Object[]> data() {
         return Arrays.asList(new Object[][] {
               {"a"}, {"ab"}, {"This is a long string."}});
      }
      
      @Parameter
      public String testString;

      @Before public void setUp() { }      
      
      @Test public void t11aLStringOfStringToString() {
         assertEquals("LString(\"" + testString + "\").toString() is wrong.",
               testString, new LString(testString).toString());
      }
      
      @Test public void t11aLStringOfStringLength() {
         assertEquals("LString(\"" + testString + "\").length() is wrong.",
               testString.length(), new LString(testString).length());
      }
   }

   @RunWith(Parameterized.class)
   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringCompareToTest {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);

      @Parameters
      public static Collection<Object[]> data() {
         return Arrays.asList(new Object[][] {
               {"abc", "abd", -1},
               {"", "a", -1}, {"a", "ab", -1}, {"abc", "abcd", -1},
               {"B", "a", -1}, {"BB", "Ba", -1},
               {"", "", 0}, {"a", "a", 0}, {"abc", "abc", 0}});
      }
      
      @Parameter(0) public String testString1;
      
      @Parameter(1) public String testString2;
      
      @Parameter(2) public int result;
      
      @Test public void t21aTestCompareTo() {
         LString testLString1 = new LString(testString1);
         LString testLString2 = new LString(testString2);
         if (result == 0) {
            assertEquals("compareTo of equal LStrings is not zero",
                  result, testLString1.compareTo(testLString2));
         } else {
            assertEquals("compareTo of \"" + testString1 + "\" and \"" + testString2 + "\" wrong",
                  result, (int)signum(testLString1.compareTo(testLString2)));
            assertEquals("compareTo of \"" + testString2 + "\" and \"" + testString1 + "\" wrong",
                  -result, (int)signum(testLString2.compareTo(testLString1)));
         }
      }

      @Test public void t22aTestEquals() {
         LString testLString1 = new LString(testString1);
         LString testLString2 = new LString(testString2);
         assertEquals("equals of \"" + testString1 + "\" and \"" + testString2 + "\" wrong",
               result == 0, testLString1.equals(testLString2));
         assertEquals("equals of \"" + testString2 + "\" and \"" + testString1 + "\" wrong",
               result == 0, testLString2.equals(testLString1));
      }
   }        
   
   @RunWith(Parameterized.class)
   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringCharAtTest {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);

      @Parameters
      public static Collection<Object[]> data() {
         return Arrays.asList(new Object[][] {
               {"a"}, {"ab"}, {"This is a longer string."}});
      }
      
      static char newChar = 'x'; // Some char not listed above.
      
      @Parameter public String testString;
      
      @Test public void t31aTestCharAt() {
         LString testLString = new LString(testString);
         assertEquals("charAt(0) is not \'" + testString.charAt(0) +"\'",
               testString.charAt(0), testLString.charAt(0));
         if (testString.length() > 1) {
            // Test the last character
            int index = testString.length() - 1;
            assertEquals("charAt(length - 1) is not \'" + testString.charAt(index) +"\'",
                  testString.charAt(index), testLString.charAt(index));
         }
         if (testString.length() > 2) {
            // Test a random character in the middle
            int index = 1 + (int)((testString.length() - 2) * random());
            assertEquals("charAt(" + index + ") is not \'" + testString.charAt(index) + "\'",
                  testString.charAt(index), testLString.charAt(index));
         }
      }
      
      @Test public void t32aTestSetCharAt() {
         LString testLString = new LString(testString);
         testLString.setCharAt(0, newChar);
         assertEquals("charAt(0) is not \'" + newChar +"\'",
               newChar, testLString.charAt(0));
         if (testString.length() > 1) {
            // Test the last character
            int index = testString.length() - 1;
            testLString.setCharAt(index, newChar);
            assertEquals("charAt(length - 1) is not \'" + newChar +"\'",
                  newChar, testLString.charAt(index));
         }
         if (testString.length() > 2) {
            // Test a random character in the middle
            int index = 1 + (int)((testString.length() - 2) * random());
            testLString.setCharAt(index, newChar);
            assertEquals("charAt(" + index + ") is not \'" + newChar + "\'",
                  newChar, testLString.charAt(index));
         }
      }
      
      @Test(expected=IndexOutOfBoundsException.class)
      public void t33aTestIndexOutOfBoundsCharAtMinus1() {
         LString testLString = new LString(testString);
         testLString.charAt(-1);
      }
      
      @Test(expected=IndexOutOfBoundsException.class)
      public void t33bTestIndexOutOfBoundsCharAtLength() {
         LString testLString = new LString(testString);
         testLString.charAt(testString.length());
      }
      
      @Test(expected=IndexOutOfBoundsException.class)
      public void t34aTestIndexOutOfBoundsSetCharAtMinus1() {
         LString testLString = new LString(testString);
         testLString.setCharAt(-1, newChar);
      }
      
      @Test(expected=IndexOutOfBoundsException.class)
      public void t34bTestIndexOutOfBoundsSetCharAtLength() {
         LString testLString = new LString(testString);
         testLString.setCharAt(testString.length(), newChar);
      }
   }

   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringSubStringTestSpecial {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);

      private LString nullLString;

      @Before public void setUp() {
         nullLString = new LString();
      }

      @Test public void test41aSubStringEmpty() {
         assertEquals("Substring of Empty LString is not Empty",
               nullLString, nullLString.substring(0, 0));
      }
             
      @Test(expected=IndexOutOfBoundsException.class)
      public void test42aSubStringException() {
         nullLString.substring(-1, 0);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test42bSubStringException() {
         nullLString.substring(0, 1);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test42cSubStringException() {
         nullLString.substring(1, 0);
      }
        
      @Test public void test43aSubStringOneChar() {
         LString testLString = new LString("a");
         assertEquals("Substring of One Character LString is not Empty",
               nullLString, testLString.substring(0, 0));
      }
      
      @Test public void test43bSubStringOneChar() {
         LString testLString = new LString("a");
         assertEquals("Substring of One Character LString is not Empty",
               nullLString, testLString.substring(1, 1));
      }
        
      @Test public void test43cSubStringOneChar() {
         LString testLString = new LString("a");
         assertEquals("Substring of One Character LString is not equals LString",
               testLString, testLString.substring(0, 1));
      }
        
      @Test public void test43dSubStringOneCharIsNew() {
         LString testLString = new LString("a");
         LString result = testLString.substring(0, 1);
         testLString.setCharAt(0, 'b');
         assertEquals("Substring of One Character LString is not new LString",
               "a", result.toString());
      }
        
      @Test(expected=IndexOutOfBoundsException.class)
      public void test44aSubStringException() {
         LString testLString = new LString("a");
         testLString.substring(-1, 0);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test44bSubStringException() {
         LString testLString = new LString("a");
         testLString.substring(0, 2);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test44cSubStringException() {
         LString testLString = new LString("a");
         testLString.substring(2, 1);
      }
   }
   
   @RunWith(Parameterized.class)
   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringSubStringTest {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);
      
      @Parameters
      public static Collection<Object[]> data() {
         return Arrays.asList(new Object[][] {
               {"ab"}, {"abc"}, {"A long string."}, {"This is an even longer string."}});
      }
      
      static final char newChar = 'x'; // Some char not listed above.
      
      @Parameter public String testString;
      
      private LString testLString;
      private LString nullLString;
      
      private int left, mid, right, length; // 0 < left <= mid <= right < length
      
      @Before public void setUp() {
         nullLString = new LString();
         testLString = new LString(testString);
         length = testString.length(); // Length of testString
         mid = length / 2; // midpoint of testString
         left = 1 + mid / 2; // index between zero and mid
         right = length - left; // index between mid and length
      }

      @Test public void test51aEmptySubstringAtStart() {
         assertEquals("substring(0, 0) is not empty LString",
            nullLString, testLString.substring(0, 0));
      }
      
      @Test public void test51bEmptySubstringAtEnd() {
         assertEquals("substring(length, length) is not empty LString",
            nullLString, testLString.substring(testString.length(), testString.length()));
      }
      
      @Test public void test51cEmptySubstringInMiddle() {
         assertEquals("substring(mid, mid) is not empty LString",
            nullLString, testLString.substring(mid, mid));
      }
      
      @Test public void test51dSubstringAtStart() {
         assertEquals("substring(0, mid) is not correct",
            testString.substring(0, mid), testLString.substring(0, mid).toString());
      }
      
      @Test public void test51eSubstringAtEnd() {
         assertEquals("substring(mid, length) is not correct",
            testString.substring(mid, length), testLString.substring(mid, length).toString());
      }
      
      @Test public void test51fSubstringInMiddle() {
         assertEquals("substring(left, right) is not correct",
            testString.substring(left, right), testLString.substring(left, right).toString());
      }
      
      @Test public void test51gSubstringAll() {
         assertEquals("substring(0, length) is not correct",
            testString.substring(0, length), testLString.substring(0, length).toString());
      }
      
      @Test public void test51hSubstringAtStartIsNew() {
         LString result = testLString.substring(0, mid);
         testLString.setCharAt((left < mid) ? left : mid - 1, newChar);
         assertEquals("substring(0, mid) is not new LString",
               testString.substring(0, mid), result.toString());
      }
      
      @Test public void test51jSubstringAtEndIsNew() {
         LString result = testLString.substring(mid, length);
         testLString.setCharAt((mid < right) ? (right - mid) : 1, newChar);
         assertEquals("substring(mid, length) is not new LString",
               testString.substring(mid, length), result.toString());
      }
      
      @Test public void test51kSubstringInMiddleIsNew() {
         if (left < right) {
            LString result = testLString.substring(left, right);
            testLString.setCharAt(mid - left, newChar);
            assertEquals("substring(left, right) is not new LString",
                  testString.substring(left, right), result.toString());
         }
      }
      
      @Test(expected=IndexOutOfBoundsException.class)
      public void test52aSubStringException() {
         testLString.substring(-1, 0);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test52bSubStringException() {
         testLString.substring(0, testString.length() + 1);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test52cSubStringException() {
         testLString.substring(mid + 1, mid);
      }
   }

   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringReplaceTestSpecial {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);
      
      private String replaceString = "xyzzy";
      
      private LString replaceLString;
      private LString nullLString;
      
      @Before public void setUp() {
         replaceLString = new LString(replaceString);
         nullLString = new LString();
      }
      
      @Test public void test61aReplaceEmptyString() {
         LString testLString = new LString();
         LString newLString = testLString.replace(0, 0, replaceLString);
         assertEquals("Replace of Empty LString is wrong",
               replaceString, newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
      }
      
      @Test(expected=IndexOutOfBoundsException.class)
      public void test62aReplaceOutOfBounds() {
         nullLString.replace(-1, 0, replaceLString);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test62bReplaceOutOfBounds() {
         nullLString.replace(0, 1, replaceLString);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test62cReplaceOutOfBounds() {
         nullLString.replace(1, 0, replaceLString);
      }
        
      @Test public void test63aReplaceOneChar() {
         LString testLString = new LString("a");
         LString newLString = testLString.replace(0, 0, replaceLString);
         assertEquals("Replace of One Character LString is wrong",
               replaceString + "a", newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
      }
      
      @Test public void test63bReplaceOneChar() {
         LString testLString = new LString("a");
         LString newLString = testLString.replace(1, 1, replaceLString);
         assertEquals("Replace of One Character LString is wrong",
               "a" + replaceString, newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
      }
        
      @Test public void test63cReplaceOneChar() {
         LString testLString = new LString("a");
         LString newLString = testLString.replace(0, 1, replaceLString);
         assertEquals("Replace of One Character LString is wrong",
               replaceString, newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
      }
        
      @Test public void test63dReplaceOneCharIsNew() {
         LString testLString = new LString("a");
         LString newLString = testLString.replace(0, 1, replaceLString);
         newLString.setCharAt(2, '!');
         assertEquals("Replace did not copy replacement LString",
               replaceString, replaceLString.toString());
      }
        
      @Test(expected=IndexOutOfBoundsException.class)
      public void test64aReplaceOutOfBounds() {
         LString testLString = new LString("a");
         testLString.replace(-1, 0, replaceLString);
      }
        
      @Test(expected=IndexOutOfBoundsException.class)
      public void test64bReplaceOutOfBounds() {
         LString testLString = new LString("a");
         testLString.replace(0, 2, replaceLString);
      }
        
      @Test(expected=IndexOutOfBoundsException.class)
      public void test64cReplaceOutOfBounds() {
         LString testLString = new LString("a");
         testLString.replace(2, 1, replaceLString);
      }
   }

   @RunWith(Parameterized.class)
   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringReplaceTest {
      // Maximum 10 milliseconds for all tests
      @Rule public Timeout timeout = new Timeout(100);
      
      @Parameters
      public static Collection<Object[]> data() {
         return Arrays.asList(new Object[][] {
               {"abcd"}, {"Yet another even longer string."}});
      }
      
      static final char newChar = '!'; // Some char not listed above.
      
      private String replaceString = "xyzzy";

      @Parameter public String testString;
      
      private LString replaceLString;
      private LString testLString;
      private LString nullLString;
      
      private int left, mid, right, length; // 0 < left <= mid <= right < length
      
      @Before public void setUp() {
         replaceLString = new LString(replaceString);
         testLString = new LString(testString);
         nullLString = new LString();
         length = testString.length(); // Length of testString
         mid = length / 2; // midpoint of testString
         left = 1 + mid / 2; // index between zero and mid
         right = length - left; // index between mid and length
      }

      @Test public void test71aReplacePrepend() {
         LString newLString = testLString.replace(0, 0, replaceLString);
         assertEquals("replace(0, 0) is wrong",
            replaceString + testString, newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
         newLString.setCharAt(2, newChar);
         assertEquals("replace did not copy replacement LString",
            replaceString, replaceLString.toString());
      }
      
      @Test public void test71bReplaceAppend() {
         LString newLString = testLString.replace(length, length, replaceLString);
         assertEquals("replace(length, length) is wrong",
            testString + replaceString, newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
         newLString.setCharAt(length + 2, newChar);
         assertEquals("replace did not copy replacement LString",
            replaceString, replaceLString.toString());
      }
      
      @Test public void test71cReplaceInMiddle() {
         LString newLString = testLString.replace(mid, mid, replaceLString);
         assertEquals("replace(mid, mid) is wrong",
            testString.substring(0, mid) + replaceString + testString.substring(mid, length), newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
         newLString.setCharAt(mid + 2, newChar);
         assertEquals("replace did not copy replacement LString",
            replaceString, replaceLString.toString());
      }
      
      @Test public void test71dReplaceAtStart() {
         LString newLString = testLString.replace(0, mid, replaceLString);
         assertEquals("replace(0, mid) is wrong",
            replaceString + testString.substring(mid, length), newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
         newLString.setCharAt(2, newChar);
         assertEquals("replace did not copy replacement LString",
            replaceString, replaceLString.toString());
      }
      
      @Test public void test71eReplaceAtEnd() {
         LString newLString = testLString.replace(mid, length, replaceLString);
         assertEquals("replace(mid, length) is wrong",
             testString.substring(0, mid) + replaceString, newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
         newLString.setCharAt(mid + 2, newChar);
         assertEquals("replace did not copy replacement LString",
            replaceString, replaceLString.toString());
      }
      
      @Test public void test71fReplaceInMiddle() {
         LString newLString = testLString.replace(left, right, replaceLString);
         assertEquals("replace(left, right) is wrong",
             testString.substring(0, left) + replaceString + testString.substring(right, length), newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
         newLString.setCharAt(left + 2, newChar);
         assertEquals("replace did not copy replacement LString",
            replaceString, replaceLString.toString());
      }
      
      @Test public void test71gReplaceAll() {
         LString newLString = testLString.replace(0, length, replaceLString);
         assertEquals("replace(0, length) is wrong",
             replaceString, newLString.toString());
         assertSame("replace returned different LString", testLString, newLString);
         newLString.setCharAt(2, newChar);
         assertEquals("replace did not copy replacement LString",
            replaceString, replaceLString.toString());
      }
      
      @Test(expected=IndexOutOfBoundsException.class)
      public void test72aReplaceException() {
         testLString.replace(-1, 0, replaceLString);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test72bReplaceException() {
         testLString.replace(0, testString.length() + 1, replaceLString);
      }

      @Test(expected=IndexOutOfBoundsException.class)
      public void test72cReplaceException() {
         testLString.replace(mid + 1, mid, replaceLString);
      }
   }

   @FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
   public static class LStringSpecialTest {
      // Maximum 1 second for special tests
      @Rule public Timeout timeout = new Timeout(1000);
      
      /* String with all legal single char code points. */
      private String allCharsString;
      
      @Before public void setUp() {
         StringBuilder sb = new StringBuilder(((int)Character.MAX_VALUE) + 10);
         sb.append("}>");
         for (char ch = Character.MIN_VALUE; ch < Character.MIN_SURROGATE; ch++)
            sb.append(ch);
         sb.append("<{");
         allCharsString = sb.toString();
      }
      
      @Test public void test81aAllChars() {
         LString testLString = new LString(allCharsString);
         assertEquals("Problem with representing all chars",
               allCharsString, testLString.toString());
      }
      
      @Test public void test81bAllCharsLength() {
         LString testLString = new LString(allCharsString);
         assertEquals("Problem with representing all chars",
               allCharsString.length(), testLString.length());
      }
      
      @Test public void test81cAllCharReplace() {
         LString testLString = new LString(allCharsString);
         LString testLString2 = new LString(allCharsString);
         int length = allCharsString.length();
         assertEquals("Problem with representing all chars",
               allCharsString + allCharsString,
               testLString.replace(length, length, testLString2).toString());
      }
   }
}
