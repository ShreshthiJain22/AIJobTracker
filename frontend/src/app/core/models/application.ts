export interface Application {
    id?: number;
    company: string;
    role: string;
    jdLink?: string;
    status: string;
    appliedDate?: string;
    source?: string;
    contactPerson?: string;
    referralRequested: boolean;
    referralRequestedDate?: string;
    referralReceived: boolean;
    emailUsed?: string;
    createdAt?: string;
    updatedAt?: string;
  }