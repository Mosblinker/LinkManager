/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package manager.painters;

import files.FilesExtended;
import files.extensions.ImageExtensions;
import icons.DebuggingIcon;
import icons.Icon2D;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *
 * @author Mosblinker
 */
public class LinkManagerPainterTester extends javax.swing.JFrame {
    /**
     * This is the name of the preference node used to store the settings for 
     * this program.
     */
    private static final String PREFERENCE_NODE_NAME = "milo/link/LinkManagerTest";
    
    private static final String DEBUG_ELEMENTS_KEY = "DebugElements";
    
    private static final String ALWAYS_SCALE_KEY = "AlwaysScale";
    
    private static final String PRINT_LISTENERS_KEY = "PrintListeners";
    
    private static final String LINK_PAINTER_SIZE_KEY = "LinkPainterSize";
    
    private static final String PAINTER_WIDTH_KEY = "PainterWidth";
    
    private static final String PAINTER_HEIGHT_KEY = "PainterHeight";
    
    private static final String SELECTED_SAVE_FILE_KEY = "SelectedSaveFile";
    /**
     * This is the key in the preference node for the directory for the save 
     * file chooser.
     */
    private static final String SAVE_FILE_CHOOSER_DIRECTORY_KEY = 
            "SaveCurrentDirectory";
    /**
     * This is the key in the preference node for the directory for the save 
     * file chooser.
     */
    private static final String ICON_FILE_CHOOSER_DIRECTORY_KEY = 
            "IconCurrentDirectory";
    
    private static final String SELECTED_PAINTER_KEY = "SelectedPainter";
    /**
     * This is an array containing the widths and heights for the icon images 
     * for this program. The icon images are generated on the fly.
     */
    private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 96, 128, 256, 512};
    /**
     * This is a template for the file names for the files to save the icon 
     * images to. This should be formatted with the image's width and height.
     */
    private static final String ICON_FILE_NAME_TEMPLATE = "%s %dx%d.png";
    /**
     * This generates a list of BufferedImages using the given Painter to be 
     * used as the icon images for this program. The 
     * width and height for the images are stored in the {@link #ICON_SIZES} 
     * array.
     * @param painter The Painter to use to generate the images.
     * @return A list of images to use as the icon images.
     * @see #ICON_SIZES
     */
    private java.util.List<BufferedImage> generateIconImages(Painter<?> painter){
            // Create a list to get the images
        ArrayList<BufferedImage> iconImages = new ArrayList<>();
            // Go through the sizes for the images
        for (int size : ICON_SIZES){
                // Create a new image that is the current size and store it
            iconImages.add(createImage(painter,size,size));
        }
        return iconImages;
    }
    /**
     * This creates and returns an image. The image will be the given width and 
     * height.
     * @param width The width for the image.
     * @param height The height for the image.
     * @return The generated image.
     */
    private BufferedImage createImage(Painter<?> painter,int width,int height){
            // This creates the placeholder image
        BufferedImage image = new BufferedImage(width, height, 
                BufferedImage.TYPE_INT_ARGB);
            // This creates the graphics for the image
        Graphics2D g = image.createGraphics();
        painter.paint(g, null, width, height);
        g.dispose();
        return image;
    }
    /**
     * This attempts to save the given image to the given file, as a png. If the 
     * image fails to save, then this will open a prompt to retry, and will 
     * try again and again as long as the image fails to save and the user wants 
     * to retry.
     * @param image The image to save.
     * @param file The file to save to.
     * @return Whether the image was successfully saved to the file.
     */
    private boolean saveImage(BufferedImage image, File file){
            // This gets whether the user wants to retry if the image fails to 
        boolean retry;  // save
        do{
            try {   // Try to save the image
                return ImageIO.write(image, "png", file);
            } catch (IOException ex) {
                System.out.println("Failed to save image: " + ex);
                    // Show the user a prompt asking if the program should try 
                    // again, and if the user says yes, then tis should try again
                retry = JOptionPane.showConfirmDialog(this, 
                        "There was an error saving the image to file\n"+
                                "\""+file+"\".\n"+
                                "Would you like to try again?"+
                                "\nError: "+ex, 
                        "Image Failed To Save", JOptionPane.YES_NO_OPTION, 
                        JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION;
            }
        }   // While the image fails to save and the user wants to try again
        while(retry);
        return false;
    }
    
    private void setSpinnerValueFromConfig(JSpinner spinner, String key, 
            double defaultValue, double mult){
        spinner.setValue(config.getDouble(key, defaultValue)*mult);
    }
    
    private void addPainter(Painter<?> painter){
        painters.add(painter);
        PainterTestIcon icon = new PainterTestIcon(painter);
        testIcons.put(painter, icon);
        debugIcons.put(painter, new DebuggingIcon(icon,debugToggle.isSelected()));
    }
    /**
     * Creates new form LinkManagerPainterTester
     */
    public LinkManagerPainterTester() {
        painters = new ArrayList<>();
        testIcons = new HashMap<>();
        debugIcons = new HashMap<>();
        initComponents();
        try{    // Try to load the settings from the preference node
            config = Preferences.userRoot().node(PREFERENCE_NODE_NAME);
            debugToggle.setSelected(config.getBoolean(DEBUG_ELEMENTS_KEY, 
                    debugToggle.isSelected()));
            viewLabel.setImageAlwaysScaled(config.getBoolean(ALWAYS_SCALE_KEY, 
                    viewLabel.isImageAlwaysScaled()));
            listenerToggle.setSelected(config.getBoolean(PRINT_LISTENERS_KEY, 
                    listenerToggle.isSelected()));
            widthSpinner.setValue(config.getInt(PAINTER_WIDTH_KEY, 512));
            heightSpinner.setValue(config.getInt(PAINTER_HEIGHT_KEY, 512));
            linkSizeToggle.setSelected(config.getBoolean(LINK_PAINTER_SIZE_KEY, 
                    Objects.equals(widthSpinner.getValue(), heightSpinner.getValue())));
            painterCombo.setSelectedIndex(config.getInt(SELECTED_PAINTER_KEY, 
                    painterCombo.getSelectedIndex()));
                // Get the name of the current directory for the save file 
                // chooser, or null
            String dirName = config.get(SAVE_FILE_CHOOSER_DIRECTORY_KEY, null);
                // If there is a current directory for the save file chooser
            if (dirName != null){
                    // Get the current directory as a File
                File dir = new File(dirName);
                    // If that file exists
                if (dir.exists())
                        // Set the save file chooser's current directory
                    fc.setCurrentDirectory(dir);
            }
                // Get the name of the current directory for the save file 
                // chooser, or null
            dirName = config.get(ICON_FILE_CHOOSER_DIRECTORY_KEY, null);
                // If there is a current directory for the save file chooser
            if (dirName != null){
                    // Get the current directory as a File
                File dir = new File(dirName);
                    // If that file exists
                if (dir.exists()){
                        // Set the save file chooser's current directory
                    iconImgFC.setCurrentDirectory(dir);
                    iconImgFC.setSelectedFile(dir);
                }
            }
        } catch (SecurityException | IllegalStateException ex){
            config = null;
            System.out.println("Unable to load settings: " +ex);
        } catch (IllegalArgumentException ex){
            System.out.println("Invalid setting: " + ex);
        }
        heightSpinner.setEnabled(!linkSizeToggle.isSelected());
        scaleToggle.setSelected(viewLabel.isImageAlwaysScaled());
        addPainter(new LinkManagerIconPainter());
        viewLabel.setIcon(getDebugIcon());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fc = new javax.swing.JFileChooser();
        iconImgFC = new javax.swing.JFileChooser();
        viewLabel = new components.JThumbnailLabel();
        jLabel5 = new javax.swing.JLabel();
        widthSpinner = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        heightSpinner = new javax.swing.JSpinner();
        linkSizeToggle = new javax.swing.JCheckBox();
        debugToggle = new javax.swing.JCheckBox();
        scaleToggle = new javax.swing.JCheckBox();
        listenerToggle = new javax.swing.JCheckBox();
        printButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        painterCombo = new javax.swing.JComboBox<>();
        saveIconButton = new javax.swing.JButton();

        fc.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        fc.setFileFilter(ImageExtensions.PNG_FILTER);

        iconImgFC.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        iconImgFC.setDialogTitle("Save Icon Images...");
        iconImgFC.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Painter Tester");

        jLabel5.setText("Width:");

        widthSpinner.setModel(new javax.swing.SpinnerNumberModel(512, -3, null, 1));
        widthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                widthSpinnerStateChanged(evt);
            }
        });

        jLabel6.setText("Height:");

        heightSpinner.setModel(new javax.swing.SpinnerNumberModel(512, -3, null, 1));
        heightSpinner.setEnabled(false);
        heightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightSpinnerStateChanged(evt);
            }
        });

        linkSizeToggle.setSelected(true);
        linkSizeToggle.setText("Link Width and Height");
        linkSizeToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkSizeToggleActionPerformed(evt);
            }
        });

        debugToggle.setText("Debug");
        debugToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugToggleActionPerformed(evt);
            }
        });

        scaleToggle.setText("Scale");
        scaleToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleToggleActionPerformed(evt);
            }
        });

        listenerToggle.setSelected(true);
        listenerToggle.setText("Print Listeners");
        listenerToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenerToggleActionPerformed(evt);
            }
        });

        printButton.setText("Print Data");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Painter:");

        painterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LinkManagerIconPainter" }));
        painterCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                painterComboActionPerformed(evt);
            }
        });

        saveIconButton.setText("Save Icon");
        saveIconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveIconButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(widthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(heightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(linkSizeToggle))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(painterCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(debugToggle)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(printButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(saveButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(scaleToggle)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(listenerToggle)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveIconButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(viewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(widthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(heightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(linkSizeToggle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(debugToggle)
                    .addComponent(saveButton)
                    .addComponent(printButton)
                    .addComponent(scaleToggle)
                    .addComponent(listenerToggle)
                    .addComponent(saveIconButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(painterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void widthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_widthSpinnerStateChanged
        if (linkSizeToggle.isSelected())
            heightSpinner.setValue(widthSpinner.getValue());
        viewLabel.repaint();
        if (config != null)
            config.putInt(PAINTER_WIDTH_KEY, (int)widthSpinner.getValue());
    }//GEN-LAST:event_widthSpinnerStateChanged

    private void heightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heightSpinnerStateChanged
        if (!linkSizeToggle.isSelected())
            viewLabel.repaint();
        if (config != null)
            config.putInt(PAINTER_HEIGHT_KEY, (int)heightSpinner.getValue());
    }//GEN-LAST:event_heightSpinnerStateChanged

    private void linkSizeToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkSizeToggleActionPerformed
        heightSpinner.setEnabled(!linkSizeToggle.isSelected());
        if (linkSizeToggle.isSelected())
            heightSpinner.setValue(widthSpinner.getValue());
        viewLabel.repaint();
        if (config != null)
            config.putBoolean(LINK_PAINTER_SIZE_KEY, linkSizeToggle.isSelected());
    }//GEN-LAST:event_linkSizeToggleActionPerformed

    private void debugToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugToggleActionPerformed
        for (DebuggingIcon debugIcon : debugIcons.values())
            debugIcon.setDebugEnabled(debugToggle.isSelected());
        viewLabel.repaint();
        if (config != null)
            config.putBoolean(DEBUG_ELEMENTS_KEY, debugToggle.isSelected());
    }//GEN-LAST:event_debugToggleActionPerformed

    private void scaleToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleToggleActionPerformed
        viewLabel.setImageAlwaysScaled(scaleToggle.isSelected());
        if (config != null)
            config.putBoolean(ALWAYS_SCALE_KEY, viewLabel.isImageAlwaysScaled());
    }//GEN-LAST:event_scaleToggleActionPerformed

    private void listenerToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenerToggleActionPerformed
        if (config != null)
            config.putBoolean(PRINT_LISTENERS_KEY, listenerToggle.isSelected());
    }//GEN-LAST:event_listenerToggleActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        System.out.println("Painter: " + getPainter());
    }//GEN-LAST:event_printButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // This will get the file that the user selected
        File file;
        do{     // Open the save file chooser and get the file the user selected
            file = showSaveFileChooser(fc);
            // If no file was selected
            if (file == null)
                return;
            // If the PNG file filter is in use and the entered file does
            // not have the PNG file extension
            if (ImageExtensions.PNG_FILTER.equals(fc.getFileFilter()) &&
                    !ImageExtensions.PNG_FILTER.accept(file)){
                // Add the PNG file extension to the file
                file = new File(file.toString()+"."+ImageExtensions.PNG);
                fc.setSelectedFile(file);
            }   // If the file already exists
            if (file.exists()){
                // Show the user a confirmation dialog asking if the user
                // wants to overwrite the file
                int option = JOptionPane.showConfirmDialog(this,
                        "There is already a file with that name.\n"+
                                "Should the file be overwritten?\n"+
                                "File: \""+file+"\"", "File Already Exists",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                // Determine the action to perform based off the user's
                switch(option){ // choice
                    // If the user selected No
                    case(JOptionPane.NO_OPTION):
                        // Set the file to null to run the loop again
                        file = null;
                    // If the user selected Yes
                    case(JOptionPane.YES_OPTION):
                        break;
                    // If the user selected Cancel or exited the dialog
                    default:
                        // Cancel the operation, and show a prompt notifying
                        // that nothing was saved.
                        JOptionPane.showMessageDialog(this,"No file was saved.",
                            "File Already Exists",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }   // While the file is null (user decided to select a different
        } while (file == null);     // file)
        if (config != null)
            config.put(SELECTED_SAVE_FILE_KEY, file.toString());
        BufferedImage image = getDebugIcon().toImage(viewLabel);
        saveImage(image,file);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void painterComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_painterComboActionPerformed
        viewLabel.setIcon(getDebugIcon());
        if (config != null)
            config.putInt(SELECTED_PAINTER_KEY, painterCombo.getSelectedIndex());
    }//GEN-LAST:event_painterComboActionPerformed

    private void saveIconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveIconButtonActionPerformed
            // Get the folder to save the icons to
        File dir = showSaveFileChooser(iconImgFC);
        displaySavedFrames(saveFrames(dir));
    }//GEN-LAST:event_saveIconButtonActionPerformed
    
    /**
     * This opens the given file chooser as a save file chooser to allow the 
     * user to select the file or folder to save to.
     * @param fc The file chooser to open.
     * @return The selected file, or null if the user canceled the file chooser.
     */
    private File showSaveFileChooser(JFileChooser fc){
            // This is used to store which button the user pressed
        int option = fc.showSaveDialog(this);
        fc.setPreferredSize(fc.getSize());
            // If the given file chooser is the normal save file chooser
        if (fc == this.fc){
            try{    // Update the file chooser's size
//                setPreferenceSize(SAVE_FILE_CHOOSER_PREFERENCE_NODE,fc.getSize());
                    // Update the file chooser's current directory
                config.put(SAVE_FILE_CHOOSER_DIRECTORY_KEY, 
                        fc.getCurrentDirectory().toString());
            }catch (IllegalStateException ex){ 
                System.out.println("Error: " + ex);
            }
        }
        else if (fc == this.iconImgFC){
            try{    // Update the file chooser's size
//                setPreferenceSize(FRAME_FILE_CHOOSER_PREFERENCE_NODE,fc.getSize());
                    // Update the file chooser's current directory
                config.put(ICON_FILE_CHOOSER_DIRECTORY_KEY, 
                        fc.getCurrentDirectory().toString());
            }catch (IllegalStateException ex){ 
                System.out.println("Error: " + ex);
            }
        }
            // If the user wants to save the file
        if (option == JFileChooser.APPROVE_OPTION)
            return fc.getSelectedFile();
        else
            return null;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LinkManagerPainterTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new LinkManagerPainterTester().setVisible(true);
        });
    }
    
    private Painter<?> getPainter(){
        int selIndex = painterCombo.getSelectedIndex();
        if (selIndex < 0 || painters.size() <= selIndex)
            return null;
        return painters.get(selIndex);
    }
    
    private DebuggingIcon getDebugIcon(){
        return debugIcons.get(getPainter());
    }
    
    private List<Painter<?>> painters;
    private Map<Painter<?>,PainterTestIcon> testIcons;
    private Map<Painter<?>,DebuggingIcon> debugIcons;
    /**
     * This is a preference node to store the settings for this program.
     */
    private Preferences config;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debugToggle;
    private javax.swing.JFileChooser fc;
    private javax.swing.JSpinner heightSpinner;
    private javax.swing.JFileChooser iconImgFC;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JCheckBox linkSizeToggle;
    private javax.swing.JCheckBox listenerToggle;
    private javax.swing.JComboBox<String> painterCombo;
    private javax.swing.JButton printButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton saveIconButton;
    private javax.swing.JCheckBox scaleToggle;
    private components.JThumbnailLabel viewLabel;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration//GEN-END:variables
    /**
     * This attempts to create the given directories, opening an Error 
     * JOptionPane if failed, and returns whether it was successful.
     * @param dir The directory to create.
     * @param existingMessage The message to display if the given directory 
     * exists as a file.
     * @param errorMessage The message to display if an error occurs.
     * @return Whether this was successful at creating the directories.
     */
    private boolean createDirectories(File dir, String existingMessage, 
            String errorMessage) {
        String message;     // The message to display
        try {
            Files.createDirectories(dir.toPath());
            return true;
        }
        catch(FileAlreadyExistsException exc) {
            message = existingMessage+" already exists as a file.";
        }
        catch (IOException | SecurityException exc) {
            message = "An error occurred while creating the "+errorMessage;
        }
        JOptionPane.showMessageDialog(this,message,
                "ERROR - Error Creating Directory",
            JOptionPane.ERROR_MESSAGE);
        return false;
    }
    /**
     * This attempts to create the given directories, opening an Error 
     * JOptionPane if failed, and returns whether it was successful.
     * @param dir The directory to create.
     * @param multiple If there will be multiple directories created.
     * @return Whether this was successful at creating the directories.
     */
    private boolean createDirectories(File dir, boolean multiple) {
        if (multiple){  // If multiple directories will be created
            return createDirectories(dir,"One of the directories",
                    "directories.");
        } else{
            return createDirectories(dir,"The specified directory",
                    "the specified directory.");
        }
    }
    /**
     * This attempts to create the given directory.
     * @param dir The directory to create.
     * @return Whether the directory was successfully created.
     */
    private boolean createDirectories(File dir){
            // If the directory file does not exist
        if (!dir.exists() || !dir.isDirectory()) {
            if (!createDirectories(dir,true))   // If the directory was not created
                return false;
        }
        return true;
    }
    
    private boolean saveFrames(File dir){
            // Try to create the directory
        if (!createDirectories(dir))   // If the directory failed to be created
            return false;
            // Generate the images and go through them 
        for (BufferedImage image : generateIconImages(getPainter())){
                // Get the file to save the image to
            File file = new File(dir,String.format(ICON_FILE_NAME_TEMPLATE, 
                    painterCombo.getSelectedItem().toString(),
                    image.getWidth(), image.getHeight()));
            if (file.exists())  // If that file already exists
                    // Get the next available file path
                file = FilesExtended.getNextAvailableFilePath(file);
                // Save the image
            if (!saveImage(image,file))
                return false;
        }
        return true;
    }
    
    private void displaySavedFrames(boolean success){
        if (success){   // If the images were successfully saved
            JOptionPane.showMessageDialog(this, 
                    "The images were successfully saved.", 
                    "Images Saved Successfully", JOptionPane.INFORMATION_MESSAGE);
        } else{
            JOptionPane.showMessageDialog(this, 
                    "The images failed to save.", 
                    "Images Failed To Save", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private class PainterTestIcon implements Icon2D{
        
        private Painter<?> painter;
        
        public PainterTestIcon(Painter<?> painter){
            this.painter = painter;
        }
        
        public Painter<?> getPainter(){
            return painter;
        }
        @Override
        public void paintIcon2D(Component c, Graphics2D g, int x, int y) {
            g = (Graphics2D) g.create();
            g.translate(x, y);
            try{
                painter.paint(g, null, getIconWidth(), getIconHeight());
            } catch (Exception ex){
                System.out.println("Error: " + ex);
            }
            g.dispose();
        }
        @Override
        public int getIconWidth() {
            if (widthSpinner == null)
                return 512;
            return (int)widthSpinner.getValue();
        }
        @Override
        public int getIconHeight() {
            if (linkSizeToggle == null || heightSpinner == null)
                return 512;
            if (linkSizeToggle.isSelected())
                return getIconWidth();
            return (int)heightSpinner.getValue();
        }
    }
}
