package net.sf.taverna.t2.uiexts.bioswr.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.DecoratorBorder;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.HtmlToolTip;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Dmitry Repchevsky
 */

public class TagLabel extends JLabel {
    
    public final static Color BACKGROUND = new Color(0xE0ECF8);
    public final static Color BACKGROUND_HIGHLITED = new Color(0xA3CEF8);
    public final static Color BACKGROUND_SELECTED = new Color(0xF6E3CE);
    
    public final static Color FOREGROUND = new Color(0x407EED);
    
    public final String reference;
    
    public boolean selected;
    
    public TagLabel(String reference) {
        this.reference = reference;
        
        setForeground(FOREGROUND);
        setBackground(BACKGROUND);
        setBorder(new DecoratorBorder(Color.LIGHT_GRAY, 10, new Insets(2,2,2,2)));
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public JToolTip createToolTip() {
        return new HtmlToolTip();
    }
        
    @Override
    public void paint(Graphics g) {
        paintBorder(g);
        g.setColor(selected ? BACKGROUND_SELECTED : getBackground());
        g.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 10, 10);
        paintComponent(g);
    }
}
