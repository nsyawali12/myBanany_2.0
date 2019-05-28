package org.apache.commons.net.ftp.parser;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.net.ftp.FTPClientConfig;

public class VMSVersioningFTPEntryParser extends VMSFTPEntryParser {
    private static final String PRE_PARSE_REGEX = "(.*?);([0-9]+)\\s*.*";
    private final Pattern _preparse_pattern_;

    public VMSVersioningFTPEntryParser() {
        this(null);
    }

    public VMSVersioningFTPEntryParser(FTPClientConfig config) {
        configure(config);
        try {
            this._preparse_pattern_ = Pattern.compile(PRE_PARSE_REGEX);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Unparseable regex supplied:  (.*?);([0-9]+)\\s*.*");
        }
    }

    public List<String> preParse(List<String> original) {
        HashMap<String, Integer> existingEntries = new HashMap();
        ListIterator<String> iter = original.listIterator();
        while (iter.hasNext()) {
            String name;
            Integer existing;
            Matcher _preparse_matcher_ = this._preparse_pattern_.matcher(((String) iter.next()).trim());
            if (_preparse_matcher_.matches()) {
                MatchResult result = _preparse_matcher_.toMatchResult();
                name = result.group(1);
                Integer nv = Integer.valueOf(result.group(2));
                existing = (Integer) existingEntries.get(name);
                if (existing == null || nv.intValue() >= existing.intValue()) {
                    existingEntries.put(name, nv);
                } else {
                    iter.remove();
                }
            }
        }
        while (iter.hasPrevious()) {
            _preparse_matcher_ = this._preparse_pattern_.matcher(((String) iter.previous()).trim());
            if (_preparse_matcher_.matches()) {
                result = _preparse_matcher_.toMatchResult();
                name = result.group(1);
                nv = Integer.valueOf(result.group(2));
                existing = (Integer) existingEntries.get(name);
                if (existing != null && nv.intValue() < existing.intValue()) {
                    iter.remove();
                }
            }
        }
        return original;
    }

    protected boolean isVersioning() {
        return true;
    }
}
