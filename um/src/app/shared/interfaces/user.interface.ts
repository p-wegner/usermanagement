export interface User {
  id: string;
  username: string;
  firstName?: string;
  lastName?: string;
  fullName: string;
  email: string;
  enabled: boolean;
  password?: string;
}

export interface UserCollection {
  id: string;
  name: string;
  description?: string;
  members: User[];
}
