package org.web25.felix.flexmark.extension.math.intern

import com.vladsch.flexmark.html.CustomNodeRenderer
import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.NodeRenderer
import com.vladsch.flexmark.html.renderer.NodeRendererContext
import com.vladsch.flexmark.html.renderer.NodeRendererFactory
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler
import com.vladsch.flexmark.util.options.DataHolder
import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGeneratorContext
import org.apache.batik.svggen.SVGGraphics2D
import org.scilab.forge.jlatexmath.DefaultTeXFont
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import org.scilab.forge.jlatexmath.cyrillic.CyrillicRegistration
import org.scilab.forge.jlatexmath.greek.GreekRegistration
import org.web25.felix.flexmark.extension.math.InlineMath
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import javax.swing.JLabel


class InlineMathNodeRenderer private constructor(): NodeRenderer {

    override fun getNodeRenderingHandlers(): MutableSet<NodeRenderingHandler<*>> {
        return mutableSetOf(NodeRenderingHandler(InlineMath::class.java, CustomNodeRenderer<InlineMath>(this::render)))
    }

    private fun render(node: InlineMath, context: NodeRendererContext, html: HtmlWriter) {
        try {
            val domImpl = GenericDOMImplementation.getDOMImplementation()
            val svgNS = "http://www.w3.org/2000/svg"
            val document = domImpl.createDocument(svgNS, "svg", null)
            val ctx = SVGGeneratorContext.createDefault(document)

            val g2 = SVGGraphics2D(ctx, true)

            DefaultTeXFont.registerAlphabet(GreekRegistration())

            val formula = TeXFormula(node.rawText.toVisibleWhitespaceString())
            val icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20f)
            icon.insets = Insets(0, 5, 0, 5)
            g2.svgCanvasSize = Dimension(icon.iconWidth, icon.iconHeight)
            g2.color = Color.white
            g2.fillRect(0, 0, icon.iconWidth, icon.iconHeight)

            val jLabel = JLabel().run {
                foreground = Color.BLACK
                this
            }

            icon.paintIcon(jLabel, g2, 0, 0)

            val buffer = ByteArrayOutputStream()
            g2.stream(OutputStreamWriter(buffer), true)
            html.withAttr().tag("img")
            html.raw(buffer.toString())
            html.tag("/img")
        } catch (t: Throwable) {
            t.printStackTrace()
            html.raw(node.rawText)
        }
    }

    class Factory : NodeRendererFactory {

        override fun create(options: DataHolder?): NodeRenderer {
            return InlineMathNodeRenderer()
        }

    }
}