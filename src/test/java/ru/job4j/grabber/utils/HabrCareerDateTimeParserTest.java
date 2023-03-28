package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

public class HabrCareerDateTimeParserTest {

    @Test
    public void whenParseTextToDate() {
        String dateText = "2023-03-28T17:01:18+03:00";
        String dateText2 = "2023-03-28T17:01:18";
        HabrCareerDateTimeParser test = new HabrCareerDateTimeParser();
        LocalDateTime parseTest = test.parse(dateText);
        Assertions.assertEquals(dateText2, parseTest.format(test.formatter));
    }
}