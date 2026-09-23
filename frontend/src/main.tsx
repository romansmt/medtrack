import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import "leaflet/dist/leaflet.css";
import "./index.css";
import { App } from "./App";
import { PatientProvider } from "./context/PatientContext";
import { UserLocationProvider } from "./context/UserLocationContext";

const queryClient = new QueryClient();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PatientProvider>
          <UserLocationProvider>
            <App />
          </UserLocationProvider>
        </PatientProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
