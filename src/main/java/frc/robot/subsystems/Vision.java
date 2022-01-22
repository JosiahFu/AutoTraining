// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static frc.robot.Constants.Vision.*;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.net.PortForwarder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Vision extends SubsystemBase {

  private final Controls m_controls;
  private final NetworkTable goal_camera;
  private final NetworkTable intake_camera;

  private CAMERA_TYPE goal_camera_type = CAMERA_TYPE.OAK_D;

  /** Creates a new Vision Subsystem. */
  public Vision(Controls controls) {
    m_controls = controls;
    goal_camera =
        NetworkTableInstance.getDefault().getTable(goal_camera_type.toString().toLowerCase());
    intake_camera = NetworkTableInstance.getDefault().getTable("OAK-1_Intake");

    PortForwarder.add(5800, goalCameraIP, 5800);
    PortForwarder.add(5801, goalCameraIP, 5801);
    PortForwarder.add(5802, intakeCameraIP, 5800);
    PortForwarder.add(5803, intakeCameraIP, 5801);
  }

  /**
   * Returns a boolean value to determine if the goal camera has a visible target.
   *
   * @return true: Goal Camera has a target. false: Goal Camera does not have a target.
   */
  public boolean getGoalValidTarget() {
    return goal_camera.getEntry("tv").getBoolean(false);
  }

  /**
   * Returns a boolean value to determine if the goal target can be used for distance calculations.
   *
   * @return true: Goal target can be used for distance calculations. false: Goal target cannot be
   *     used for distance calculations.
   */
  public boolean getGoalGoodTarget() {
    return goal_camera.getEntry("tv").getBoolean(false);
  }

  /**
   * Returns the vertical angle of the goal target in degrees.
   *
   * @return Vertical angle (+/- 20 degrees)
   */
  public double getGoalTargetXAngle() {
    return goal_camera.getEntry("tx").getDouble(0);
  }

  /**
   * Returns the horizontal angle of the goal target in degrees.
   *
   * @return Horizontal angle (+/- 20 degrees)
   */
  public double getGoalTargetYAngle() {
    return goal_camera.getEntry("ty").getDouble(0);
  }

  /**
   * Returns the distance of the goal target in meters
   *
   * @return Direct Distance to goal target (0-30 meters)
   */
  public double getGoalTargetDirectDistance() {
    return goal_camera.getEntry("tz").getDouble(0);
  }

  /**
   * Calculates the horizontal distance to the goal target using the Upper Hub.
   *
   * @return Horizontal Distance to goal target (0-30 meters)
   */
  public double getGoalTargetHorizontalDistance() {
    return Math.cos(Units.degreesToRadians(CAMERA_MOUNTING_ANGLE_DEGREES + getGoalTargetYAngle()))
        * getGoalTargetDirectDistance();
  }

  /**
   * Returns a boolean value for the intake target state
   *
   * @return true: Intake Camera has a target. false: Intake Camera does not have a target.
   */
  public int getIntakeTargetsValid() {
    return (int) goal_camera.getEntry("tv").getNumber(0);
  }

  /**
   * Returns the angle of the intake target in degrees.
   *
   * @param targetIndex value of the target index in descending order, with 0 being the most valid
   *     target.
   * @return +/- 20 degrees
   */
  public double getIntakeTargetAngle(int targetIndex) {
    double[] nullValue = {-99};
    var intakeAngles = goal_camera.getEntry("tx").getDoubleArray(nullValue);
    try {
      return intakeAngles[0] == -99 ? 0 : intakeAngles[targetIndex];
    } catch (Exception e) {
      System.out.println("Vision Subsystem Error: getIntakeTargetAngle() illegal array access");
      return 0;
    }
  }

  /**
   * Set the state of the Goal Camera LEDs. Does not do anything if the goal camera is an OAK
   * device.
   *
   * @param state true: Set LEDs On false: set LEDs Off
   */
  public void setGoalCameraLedState(boolean state) {
    int ledMode = 0;
    if (state) {
      switch (goal_camera_type) {
        case LIMELIGHT:
          ledMode = 3;
          break;
        case PHOTONVISION:
          ledMode = 1;
          break;
        case OAK_D:
        default:
          ledMode = 0;
          break;
      }
    } else {
      switch (goal_camera_type) {
        case LIMELIGHT:
          ledMode = 1;
          break;
        case PHOTONVISION:
        case OAK_D:
        default:
          ledMode = 0;
          break;
      }
    }

    goal_camera.getEntry("ledMode").setNumber(ledMode);
  }

  /** Sends values to SmartDashboard */
  private void updateSmartDashboard() {
    SmartDashboard.putBoolean("Has Goal Target", getGoalValidTarget());
    SmartDashboard.putNumber("Goal Angle", getGoalTargetXAngle());
    SmartDashboard.putNumber("Goal Direct Distance", getGoalTargetDirectDistance());
    SmartDashboard.putNumber("Goal Horizontal Distance", getGoalTargetHorizontalDistance());

    SmartDashboard.putBoolean("Has Intake Target", getIntakeTargetsValid() > 0);
    SmartDashboard.putNumber("Intake Angle", getIntakeTargetAngle(0));
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    updateSmartDashboard();
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
