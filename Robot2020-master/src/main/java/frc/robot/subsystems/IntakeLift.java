package frc.robot.subsystems;

import frc.robot.Constants;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeLift extends SubsystemBase{
    private final DoubleSolenoid m_solenoid = new DoubleSolenoid(Constants.Pneumatics.SOLENOID_1_ON, Constants.Pneumatics.SOLENOID_1_OFF);

    public void off(){
        m_solenoid.set(Value.kOff);
    }
    public void up(){
        m_solenoid.set(Value.kForward);
    }
    public void down(){
        m_solenoid.set(Value.kReverse);
    }
}