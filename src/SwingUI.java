import ru.gremal.cs.common.ui.AbstractUIControl;

import javax.swing.*;

/**
 * Классы, инкапсулирующие унифицирующие методы абстрактного элемента пользовательского интерфейса в реальные элементы текущего интерфейса (в данном случае, в элементы интерфейса, построенного на основании Swing), т. о. приводящие специфические элементы текущего интерфейса к унифицированному виду.
 */
public class SwingUI {
    public static class STextField extends JTextField implements AbstractUIControl {
        // счётчик событий о движении курсора. Используется в подсчёте запущеных нитей.
        private volatile int changeEventCounter = 0;
        @Override
        public void setSText(String text) {
            this.setText(text);
        }

        // private void setText(String text){ super.setText(text); }

        @Override
        public String getSText(){
            return this.getText();
        }

        @Override
        public void setSEnable() {
            this.setEnabled(true);
        }

        @Override
        public void setSDisable() {
            this.setEnabled(false);
        }

        @Override
        public boolean isSEnable() {
            return this.isEnabled();
        }

        protected synchronized int getChangeEventCounter(){ return this.changeEventCounter; }
        protected synchronized void incChangeEventCounter() { this.changeEventCounter++; }
        protected synchronized int decChangeEventCounter() { return this.changeEventCounter--; }
    }
    public static class SButton extends JButton implements AbstractUIControl{
        //public SButton(String text){}
        @Override
        public void setSText(String text) {
            this.setText(text);
        }

        @Override
        public String getSText(){
            return this.getText();
        }

        @Override
        public void setSEnable() {
            this.setEnabled(true);
        }

        @Override
        public void setSDisable() {
            this.setEnabled(false);
        }

        @Override
        public boolean isSEnable() {
            return this.isEnabled();
        }
    }

    public static class STextArea extends JTextArea implements AbstractUIControl{
        // счётчик событий о движении курсора
        private volatile int changeEventCounter = 0;
        @Override
        public void setSText(String text) {
            this.setText(text);
        }

        @Override
        public String getSText(){
            return this.getText();
        }
        @Override
        public void setSEnable() {
            this.setEnabled(true);
        }

        @Override
        public void setSDisable() {
            this.setEnabled(false);
        }

        @Override
        public boolean isSEnable() {
            return this.isEnabled();
        }

        protected synchronized int getChangeEventCounter(){ return this.changeEventCounter; }
        protected synchronized void incChangeEventCounter() { this.changeEventCounter++; }
        protected synchronized int decChangeEventCounter() { return this.changeEventCounter--; }
    }
}
