import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Signup } from './features/auth/signup/signup';
import { VerifyEmail } from './features/auth/verify-email/verify-email';
import { ForgotPassword } from './features/auth/forgot-password/forgot-password';
import { ResetPassword } from './features/auth/reset-password/reset-password';
import { GoalsSetup } from './features/onboarding/goals-setup/goals-setup';
import { ApplicationsTable } from './features/applications/applications-table/applications-table';
import { authGuard } from './core/guards/auth-guard';
import { settingsGuard } from './core/guards/settings-guard';
import { ColumnsSetup } from './features/settings/columns-setup/columns-setup';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'signup', component: Signup },
  { path: 'verify', component: VerifyEmail },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword },
  { path: 'columns-setup', component: ColumnsSetup, canActivate: [authGuard] },
  { path: 'goals-setup', component: GoalsSetup, canActivate: [authGuard] },
  { path: '', component: ApplicationsTable, canActivate: [authGuard, settingsGuard] },
];