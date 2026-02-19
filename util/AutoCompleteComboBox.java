package projeksmt2.util;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class AutoCompleteComboBox extends JComboBox<String> {
    private List<String> originalItems;
    private boolean isAdjusting = false;
    private JTextField textField;

    public AutoCompleteComboBox() {
        super();
        setEditable(true);
        originalItems = new ArrayList<>();
        textField = (JTextField) getEditor().getEditorComponent();
        textField.setText("");
        
        // Listener untuk pengetikan
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!isAdjusting) {
                    String text = textField.getText();
                    filterItems(text);
                }
            }
        });
        
        // Mencegah auto-selection
        addActionListener(e -> {
            if (!isAdjusting && getSelectedItem() != null) {
                String selected = getSelectedItem().toString();
                if (!selected.equals(textField.getText())) {
                    textField.setText(selected);
                }
            }
        });
    }

    public void setItemList(List<String> items) {
        originalItems.clear();
        originalItems.addAll(items);
    }

    private void filterItems(String text) {
        isAdjusting = true;
        
        // Simpan state
        String currentText = textField.getText();
        int caretPos = textField.getCaretPosition();
        
        // Filter item
        List<String> filteredItems = new ArrayList<>();
        if (!text.isEmpty()) {
            String lowerText = text.toLowerCase();
            for (String item : originalItems) {
                if (item.toLowerCase().contains(lowerText)) {
                    filteredItems.add(item);
                    if (filteredItems.size() >= 5) break;
                }
            }
        }
        
        // Update dropdown
        removeAllItems();
        for (String item : filteredItems) {
            addItem(item);
        }
        
        // Kembalikan teks asli
        setSelectedItem(null);
        textField.setText(currentText);
        textField.setCaretPosition(caretPos);
        
        // Toggle popup
        if (!filteredItems.isEmpty() && !text.isEmpty()) {
            showPopup();
        } else {
            hidePopup();
        }
        
        isAdjusting = false;
    }
}