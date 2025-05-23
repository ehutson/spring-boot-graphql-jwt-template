package dev.ehutson.template.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;

import java.util.List;
import java.util.Map;

public class CRLFLogConverter extends CompositeConverter<ILoggingEvent> {

    public static final Marker CRLF_SAFE_MARKER = MarkerFactory.getMarker("CRLF_SAFE");

    private static final String[] SAFE_LOGS = {
            "org.hibernate",
            "org.springframework.boot.autoconfigure",
            "org.springframework.boot.diagnostics",
    };

    private static final Map<String, AnsiElement> ELEMENTS;

    static {
        ELEMENTS = Map.of(
                "faint", AnsiStyle.FAINT,
                "red", AnsiColor.RED,
                "green", AnsiColor.GREEN,
                "yellow", AnsiColor.YELLOW,
                "blue", AnsiColor.BLUE,
                "magenta", AnsiColor.MAGENTA,
                "cyan", AnsiColor.CYAN
        );
    }

    @Override
    protected String transform(ILoggingEvent event, String in) {
        AnsiElement element = ELEMENTS.get(getFirstOption());
        List<Marker> markers = event.getMarkerList();
        if ((markers != null && !markers.isEmpty() && markers.getFirst().contains(CRLF_SAFE_MARKER)) || isSafe(event)) {
            return in;
        }

        String replacement = element == null ? "_" : toAnsiString("_", element);
        return in.replaceAll("[\n\r\t]", replacement);
    }

    protected boolean isSafe(ILoggingEvent event) {
        for (String safe : SAFE_LOGS) {
            if (event.getMessage().contains(safe)) {
                return true;
            }
        }
        return false;
    }

    protected String toAnsiString(String in, AnsiElement element) {
        return AnsiOutput.toString(element, in);
    }
}
