package frc.robot.commands;

import frc.robot.subsystems.FlyWheel;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class FlyWheelSpin extends CommandBase {
	private final FlyWheel m_flyWheel;

	public FlyWheelSpin(final FlyWheel flyWheel) {
		m_flyWheel = flyWheel;
		addRequirements(m_flyWheel);
	}

	public void initialize() {
		m_flyWheel.setRPM(120);
	}

	public void execute() {
		m_flyWheel.setFlyWheel();
	}

	public boolean isFinished() {
		return false;
	}

	public void stop() {
		m_flyWheel.stop();
	}
}
