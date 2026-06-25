import { forwardJsonRequest } from "@/lib/backend";

export function POST(request: Request) {
  return forwardJsonRequest(request, "/api/auth/login");
}
