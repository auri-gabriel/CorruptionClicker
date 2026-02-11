package com.aurigabriel;

import javax.swing.SwingUtilities;

import com.aurigabriel.ui.GameUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameUI::new);
    }
}