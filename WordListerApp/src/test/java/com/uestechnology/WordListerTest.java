package com.uestechnology;


import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple WordLister.
 */
public class WordListerTest {


    @Test
    public void testGetMaxFourLetters() {
        int result = WordLister.getMax("ABCD");
        Assert.assertEquals(result, 4321);
    }

    @Test
    public void testGetMaxFiveLetters() {
        int result = WordLister.getMax("CABCD");
        Assert.assertEquals(result, 54321);
    }

    @Test
    public void testGetMaxThreeLetters() {
        int result = WordLister.getMax("BCD");
        Assert.assertEquals(result, 321);
    }


    @Test
    public void testGetMaxTwoLetters() {
        int result = WordLister.getMax("CD");
        Assert.assertEquals(result, 21);
    }

    @Test
    public void testGetMaxZeroLetters() {
        int result = WordLister.getMax("");
        Assert.assertEquals(result, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMaxNegativeLetters() {
        int result = WordLister.getMax(null);
        Assert.assertEquals(result, 0);
    }

    @Test
    public void testGetMinFourLetters() {
        int result = WordLister.getMin("ABCD");
        Assert.assertEquals(result, 1234);
    }

    @Test
    public void testGetMinFiveLetters() {
        int result = WordLister.getMin("CABCD");
        Assert.assertEquals(result, 12345);
    }

    @Test
    public void testGetMinThreeLetters() {
        int result = WordLister.getMin("BCD");
        Assert.assertEquals(result, 123);
    }


    @Test
    public void testGetMinTwoLetters() {
        int result = WordLister.getMin("CD");
        Assert.assertEquals(result, 12);
    }

    @Test
    public void testGetMinZeroLetters() {
        int result = WordLister.getMin("");
        Assert.assertEquals(result, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMinNegativeLetters() {
        int result = WordLister.getMin(null);
        Assert.assertEquals(result, 0);
    }

    //    checkValid(Integer number, Integer reference)
    @Test
    public void testValidWord() {
        Assert.assertTrue(WordLister.checkValid(1234, 1234));
    }


    @Test
    public void testInvalidWordDuplicates() {
        Assert.assertFalse(WordLister.checkValid(1224, 1234));
    }

    @Test
    public void testInvalidWordRange() {
        Assert.assertFalse(WordLister.checkValid(1235, 1234));
    }

    @Test
    public void testInvalidWordRange2(){
        Assert.assertFalse(WordLister.checkValid(1249, 1234));
    }

}
