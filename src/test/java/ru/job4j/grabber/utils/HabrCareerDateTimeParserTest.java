package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HabrCareerDateTimeParserTest {

    @Test
    public void whenParseTextToDate() {
        String dateText = "2023-03-28T19:43:23+03:00";
        String expected = "2023-03-28T19:43:23";
        var result = new HabrCareerDateTimeParser().parse(dateText);
        assertThat(expected).isEqualTo(result.toString());
    }
}