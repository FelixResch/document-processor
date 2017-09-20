package org.web25.felix.flexmark.extension.math.intern

import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.internal.Delimiter
import com.vladsch.flexmark.parser.InlineParser
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor
import com.vladsch.flexmark.parser.delimiter.DelimiterRun
import com.vladsch.flexmark.util.sequence.BasedSequence
import org.web25.felix.flexmark.extension.math.InlineMath

class InlineMathDelimiterProcessor : DelimiterProcessor {

    override fun canBeCloser(leftFlanking: Boolean, rightFlanking: Boolean, beforeIsPunctuation: Boolean, afterIsPunctuation: Boolean, beforeIsWhitespace: Boolean, afterIsWhiteSpace: Boolean): Boolean = rightFlanking

    override fun getClosingCharacter(): Char = '$'

    override fun getDelimiterUse(opener: DelimiterRun?, closer: DelimiterRun?): Int {
        opener!!
        closer!!
        if (opener.length() >= 2 && closer.length() >= 2) {
            return 2;
        } else {
            return 0;
        }
    }

    override fun getMinLength(): Int = 2

    override fun canBeOpener(leftFlanking: Boolean, rightFlanking: Boolean, beforeIsPunctuation: Boolean, afterIsPunctuation: Boolean, beforeIsWhitespace: Boolean, afterIsWhiteSpace: Boolean): Boolean = leftFlanking

    override fun getOpeningCharacter(): Char = '$'

    override fun process(opener: Delimiter?, closer: Delimiter?, delimitersUsed: Int) {
        opener!!
        closer!!
        val inlineMath = InlineMath(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getTailChars(delimitersUsed))
        opener.moveNodesBetweenDelimitersTo(inlineMath, closer)
    }

    override fun unmatchedDelimiterNode(inlineParser: InlineParser?, delimiter: DelimiterRun?): Node? = null

}