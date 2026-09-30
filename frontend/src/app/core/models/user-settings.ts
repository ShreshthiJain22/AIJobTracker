export interface UserSettings {
  id?: number;
  goals: string[];
  reminderLanguage: string;
  remindersEnabled: boolean;
  reminderTime: string;
  reminderFrequency: string;
  visibleFields?: string[];
  gender?: string;
}