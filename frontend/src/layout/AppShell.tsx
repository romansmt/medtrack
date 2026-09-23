import { useState } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { Icon } from "./Icon";
import { PatientSelector } from "./PatientSelector";
import { allNavItems, primaryNavItems, secondaryNavItems } from "./navItems";
import "./AppShell.css";

export function AppShell() {
  const [moreOpen, setMoreOpen] = useState(false);

  return (
    <div className="app-shell">
      <header className="app-header">
        <span className="app-logo">MedTrack</span>
        <nav className="desktop-nav" aria-label="Hauptnavigation">
          {allNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => "desktop-nav__link" + (isActive ? " is-active" : "")}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <PatientSelector />
      </header>

      <main className="app-main">
        <Outlet />
      </main>

      <nav className="bottom-nav" aria-label="Hauptnavigation">
        {primaryNavItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => "bottom-nav__item" + (isActive ? " is-active" : "")}
            onClick={() => setMoreOpen(false)}
          >
            <Icon name={item.icon} />
            <span>{item.label}</span>
          </NavLink>
        ))}
        <button
          type="button"
          className={"bottom-nav__item bottom-nav__item--button" + (moreOpen ? " is-active" : "")}
          onClick={() => setMoreOpen((open) => !open)}
          aria-expanded={moreOpen}
          aria-label="Mehr"
        >
          <Icon name="more" />
          <span>Mehr</span>
        </button>
      </nav>

      {moreOpen && (
        <>
          <div className="more-sheet-backdrop" onClick={() => setMoreOpen(false)} />
          <div className="more-sheet" role="menu">
            {secondaryNavItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => "more-sheet__item" + (isActive ? " is-active" : "")}
                onClick={() => setMoreOpen(false)}
              >
                <Icon name={item.icon} />
                <span>{item.label}</span>
              </NavLink>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
