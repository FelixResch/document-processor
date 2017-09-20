package org.web25.felix.flexmark.extension.math

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataHolder
import org.web25.felix.flexmark.extension.math.intern.InlineMathDelimiterProcessor
import org.web25.felix.flexmark.extension.math.intern.InlineMathNodeRenderer

class MathExtension private constructor() : Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {


    override fun extend(parserBuilder: Parser.Builder?) {
        parserBuilder!!
        parserBuilder.customDelimiterProcessor(InlineMathDelimiterProcessor())
    }

    override fun parserOptions(options: MutableDataHolder?) {
    }

    override fun extend(rendererBuilder: HtmlRenderer.Builder?, rendererType: String?) {
        if(rendererType != null && (rendererType == "JIRA" || rendererType == "YOUTRACK")) {
            TODO("not yet implemented")
        } else {        //Default to HTML
            rendererBuilder?.nodeRendererFactory(InlineMathNodeRenderer.Factory())
        }
    }

    override fun rendererOptions(options: MutableDataHolder?) {
    }

    companion object {

        @JvmStatic
        fun create(): MathExtension {
            return MathExtension()
        }
    }
}