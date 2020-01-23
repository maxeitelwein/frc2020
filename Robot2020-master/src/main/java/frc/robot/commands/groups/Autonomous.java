package frc.robot.commands.groups;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.AutoDrive;
import frc.robot.subsystems.DriveBase;

public class Autonomous extends SequentialCommandGroup{
    private final DriveBase m_driveBase;
    public Autonomous(DriveBase driveBase){
        m_driveBase = driveBase;
        addCommands(new AutoDrive(m_driveBase));
    }
}
