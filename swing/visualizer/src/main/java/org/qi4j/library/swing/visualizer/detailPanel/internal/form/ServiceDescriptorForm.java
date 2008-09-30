/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.swing.visualizer.detailPanel.internal.form;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @see org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor
 * @since 0.5
 */
public final class ServiceDescriptorForm
{
    private JPanel placeHolder;

    private JComponent serviceSeparator;
    private JTextField serviceId;
    private JTextField serviceType;
    private JCheckBox serviceIsInstantiateAtStartup;
    private JTextField serviceVisibility;
    private JComponent locationSeparator;
    private JList serviceAccessibleBy;

    private JTextField layer;
    private JTextField module;

    public final void updateModel( ServiceDetailDescriptor aDescriptor )
    {
        populateServiceFields( aDescriptor );
        populateLocationFields( aDescriptor );
    }

    @SuppressWarnings( "unchecked" )
    private void populateServiceFields( ServiceDetailDescriptor aDescriptor )
    {
        String identity = null;
        boolean instantiateOnStartup = false;
        String visibility = null;
        String className = null;
        ListModel accessibleToLayers = null;

        if( aDescriptor != null )
        {
            ServiceDescriptor descriptor = aDescriptor.descriptor();
            identity = descriptor.identity();
            className = descriptor.type().getName();
            instantiateOnStartup = descriptor.isInstantiateOnStartup();
            visibility = descriptor.visibility().toString();

            final List<LayerDetailDescriptor> detailDescriptors = aDescriptor.accessibleToLayers();
            accessibleToLayers = new ListListModel( detailDescriptors );
        }

        serviceId.setText( identity );
        serviceType.setText( className );
        serviceIsInstantiateAtStartup.setSelected( instantiateOnStartup );
        serviceVisibility.setText( visibility );
        serviceAccessibleBy.setModel( accessibleToLayers );
    }

    private void populateLocationFields( ServiceDetailDescriptor aDescriptor )
    {
        String moduleName = null;
        String layerName = null;

        if( aDescriptor != null )
        {
            ModuleDetailDescriptor moduleDD = aDescriptor.module();
            moduleName = moduleDD.descriptor().name();
            LayerDetailDescriptor layerDD = moduleDD.layer();
            layerName = layerDD.descriptor().name();
        }

        module.setText( moduleName );
        layer.setText( layerName );
    }

    private void createUIComponents()
    {
        DefaultComponentFactory cmpFactory = DefaultComponentFactory.getInstance();
        serviceSeparator = cmpFactory.createSeparator( "Service" );
        locationSeparator = cmpFactory.createSeparator( "Location" );
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        createUIComponents();
        placeHolder = new JPanel();
        placeHolder.setLayout( new FormLayout( "fill:max(d;4px):noGrow,fill:p:noGrow,fill:max(d;4px):noGrow,fill:max(m;160dlu):noGrow", "center:max(d;4px):noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:p:noGrow,top:5dlu:noGrow,center:p:noGrow,top:4dlu:noGrow,center:p:noGrow,top:4dlu:noGrow,center:p:noGrow,top:4dlu:noGrow,top:max(m;50dlu):noGrow,top:5dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:p:noGrow,top:4dlu:noGrow,center:max(p;4px):noGrow" ) );
        ( (FormLayout) placeHolder.getLayout() ).setRowGroups( new int[][]{ new int[]{ 4, 6, 8, 10 }, new int[]{ 2, 14 }, new int[]{ 16, 18 } } );
        final JLabel label1 = new JLabel();
        label1.setText( "Id" );
        CellConstraints cc = new CellConstraints();
        placeHolder.add( label1, cc.xy( 2, 4 ) );
        serviceId = new JTextField();
        serviceId.setEditable( false );
        placeHolder.add( serviceId, cc.xy( 4, 4 ) );
        final JLabel label2 = new JLabel();
        label2.setText( "Class name" );
        placeHolder.add( label2, cc.xy( 2, 6 ) );
        final JLabel label3 = new JLabel();
        label3.setText( "Is instatiate at startup" );
        placeHolder.add( label3, cc.xy( 2, 10 ) );
        serviceIsInstantiateAtStartup = new JCheckBox();
        serviceIsInstantiateAtStartup.setEnabled( false );
        serviceIsInstantiateAtStartup.setText( "" );
        placeHolder.add( serviceIsInstantiateAtStartup, cc.xy( 4, 10, CellConstraints.LEFT, CellConstraints.DEFAULT ) );
        serviceType = new JTextField();
        serviceType.setEditable( false );
        placeHolder.add( serviceType, cc.xy( 4, 6 ) );
        final JLabel label4 = new JLabel();
        label4.setText( "Visiblity" );
        placeHolder.add( label4, cc.xy( 2, 8 ) );
        serviceVisibility = new JTextField();
        serviceVisibility.setEditable( false );
        placeHolder.add( serviceVisibility, cc.xy( 4, 8 ) );
        placeHolder.add( serviceSeparator, cc.xyw( 2, 2, 3 ) );
        placeHolder.add( locationSeparator, cc.xyw( 2, 14, 3 ) );
        final JLabel label5 = new JLabel();
        label5.setText( "Layer" );
        placeHolder.add( label5, cc.xy( 2, 16 ) );
        layer = new JTextField();
        layer.setEditable( false );
        placeHolder.add( layer, cc.xy( 4, 16 ) );
        final JLabel label6 = new JLabel();
        label6.setText( "Module" );
        placeHolder.add( label6, cc.xy( 2, 18 ) );
        module = new JTextField();
        module.setEditable( false );
        placeHolder.add( module, cc.xy( 4, 18 ) );
        final JLabel label7 = new JLabel();
        label7.setText( "Accessible by (layer)" );
        placeHolder.add( label7, cc.xy( 2, 12 ) );
        serviceAccessibleBy = new JList();
        serviceAccessibleBy.setSelectionMode( 0 );
        serviceAccessibleBy.setVisibleRowCount( 5 );
        placeHolder.add( serviceAccessibleBy, cc.xy( 4, 12, CellConstraints.DEFAULT, CellConstraints.FILL ) );
        label1.setLabelFor( serviceId );
        label2.setLabelFor( serviceType );
        label3.setLabelFor( serviceIsInstantiateAtStartup );
        label4.setLabelFor( serviceVisibility );
        label5.setLabelFor( layer );
        label6.setLabelFor( module );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return placeHolder;
    }
}
