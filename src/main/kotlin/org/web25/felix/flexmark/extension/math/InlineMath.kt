package org.web25.felix.flexmark.extension.math

import com.vladsch.flexmark.ast.CustomNode
import com.vladsch.flexmark.ast.DelimitedNode
import com.vladsch.flexmark.ast.DoNotDecorate
import com.vladsch.flexmark.util.sequence.BasedSequence

class InlineMath(var opener : BasedSequence, var rawText: BasedSequence, var closer: BasedSequence)
    : CustomNode(opener.baseSubSequence(opener.startOffset, closer.endOffset)), DelimitedNode, DoNotDecorate {

    override fun getSegments(): Array<BasedSequence> = arrayOf(opener, rawText, closer)

    override fun getClosingMarker(): BasedSequence = closer

    override fun setOpeningMarker(openingMarker: BasedSequence?) {
        if(openingMarker != null) {
            this.opener = openingMarker
        }
    }

    override fun setClosingMarker(closingMarker: BasedSequence?) {
        if(closingMarker != null) {
            this.closer = closingMarker
        }
    }

    override fun getOpeningMarker(): BasedSequence = opener

    override fun getText(): BasedSequence = rawText

    override fun setText(text: BasedSequence?) {
        if (text != null) {
            this.rawText = text
        }
    }
}