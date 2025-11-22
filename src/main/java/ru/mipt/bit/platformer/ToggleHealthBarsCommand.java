package ru.mipt.bit.platformer;

public class ToggleHealthBarsCommand implements Command {
    @Override
    public void execute() {
        HealthBarDecorator.toggleHealthBars();
    }
    
    @Override
    public boolean canExecute() {
        return true;
    }
}
